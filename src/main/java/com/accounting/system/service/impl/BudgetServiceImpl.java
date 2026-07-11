package com.accounting.system.service.impl;

import com.accounting.system.exception.BusinessException;
import com.accounting.system.model.dto.BudgetDTO;
import com.accounting.system.model.entity.Budget;
import com.accounting.system.model.entity.Category;
import com.accounting.system.model.vo.BudgetVO;
import com.accounting.system.repository.BillRepository;
import com.accounting.system.repository.BudgetRepository;
import com.accounting.system.repository.CategoryRepository;
import com.accounting.system.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * 预算服务实现
 * ============================================================
 *
 * 【核心机制】
 * 每个预算记录保存两条金额：
 * - budgetAmount：用户设定的预算上限
 * - spentAmount：实时计算的已支出金额（每次查询从账单表聚合）
 *
 * 【isOverBudget】
 * 超预算标记 = spentAmount > budgetAmount，前端可用此字段切换警告色
 *
 * 【Upsert模式】
 * setBudget 使用"存在则更新，不存在则创建"的 upsert 逻辑：
 * 1. 先查询已存在的预算
 * 2. 如果存在，更新金额
 * 3. 如果不存在，build新实体
 * 这样避免了重复创建预算记录
 */
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final BillRepository billRepository;

    /**
     * 设置预算（创建或更新）
     * 支持分类预算（categoryId 不为null）和总预算（categoryId 为null）
     */
    @Override
    @Transactional
    public BudgetVO setBudget(Long userId, BudgetDTO dto) {
        Budget budget;

        if (dto.getCategoryId() != null) {
            budget = budgetRepository.findByUserIdAndYearAndMonthAndCategoryId(
                    userId, dto.getYear(), dto.getMonth(), dto.getCategoryId())
                    .orElse(Budget.builder()
                            .userId(userId)
                            .year(dto.getYear())
                            .month(dto.getMonth())
                            .category(categoryRepository.findById(dto.getCategoryId())
                                    .orElseThrow(() -> new BusinessException("分类不存在")))
                            .budgetAmount(BigDecimal.ZERO)
                            .spentAmount(BigDecimal.ZERO)
                            .build());
        } else {
            budget = budgetRepository.findByUserIdAndYearAndMonthAndCategoryIsNull(
                    userId, dto.getYear(), dto.getMonth())
                    .orElse(Budget.builder()
                            .userId(userId)
                            .year(dto.getYear())
                            .month(dto.getMonth())
                            .budgetAmount(BigDecimal.ZERO)
                            .spentAmount(BigDecimal.ZERO)
                            .build());
        }

        budget.setBudgetAmount(dto.getBudgetAmount());

        // Recalculate spent amount
        BigDecimal spent;
        if (dto.getCategoryId() != null) {
            spent = billRepository.sumExpenseByUserAndCategoryAndMonth(
                    userId, dto.getCategoryId(), dto.getYear(), dto.getMonth());
        } else {
            spent = billRepository.sumExpenseByUserAndMonth(
                    userId, dto.getYear(), dto.getMonth());
        }

        if (spent == null) spent = BigDecimal.ZERO;
        budget.setSpentAmount(spent);
        budget.setIsOverBudget(spent.compareTo(dto.getBudgetAmount()) > 0);

        budget = budgetRepository.save(budget);
        return toBudgetVO(budget);
    }

    /**
     * 获取预算列表
     * 每次查询都会实时更新 spentAmount，确保数据一致性
     */
    @Override
    public List<BudgetVO> getBudgets(Long userId, int year, int month) {
        List<Budget> budgets = budgetRepository.findByUserIdAndYearAndMonth(userId, year, month);

        // Update spent amounts
        budgets.forEach(b -> {
            BigDecimal spent;
            if (b.getCategory() != null) {
                spent = billRepository.sumExpenseByUserAndCategoryAndMonth(
                        userId, b.getCategory().getId(), year, month);
            } else {
                spent = billRepository.sumExpenseByUserAndMonth(userId, year, month);
            }
            if (spent == null) spent = BigDecimal.ZERO;
            b.setSpentAmount(spent);
            b.setIsOverBudget(spent.compareTo(b.getBudgetAmount()) > 0);
        });

        return budgets.stream().map(this::toBudgetVO).collect(Collectors.toList());
    }

    /**
     * 获取预算使用率 —— 前端进度条的数据源
     * 返回每个预算的已花费/预算金额比例信息
     */
    @Override
    public List<BudgetVO> getBudgetUsage(Long userId, int year, int month) {
        List<Budget> budgets = budgetRepository.findByUserIdAndYearAndMonth(userId, year, month);

        // Also add budgets for each category that has spending but no explicit budget
        List<BudgetVO> result = new ArrayList<>();

        for (Budget b : budgets) {
            BigDecimal spent;
            if (b.getCategory() != null) {
                spent = billRepository.sumExpenseByUserAndCategoryAndMonth(
                        userId, b.getCategory().getId(), year, month);
            } else {
                spent = billRepository.sumExpenseByUserAndMonth(userId, year, month);
            }
            if (spent == null) spent = BigDecimal.ZERO;
            b.setSpentAmount(spent);
            b.setIsOverBudget(spent.compareTo(b.getBudgetAmount()) > 0);
            result.add(toBudgetVO(b));
        }

        return result;
    }

    private BudgetVO toBudgetVO(Budget budget) {
        BigDecimal remaining = budget.getBudgetAmount().subtract(budget.getSpentAmount());
        double usagePercent = budget.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0
                ? budget.getSpentAmount().divide(budget.getBudgetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0;

        return BudgetVO.builder()
                .id(budget.getId())
                .categoryId(budget.getCategory() != null ? budget.getCategory().getId() : null)
                .categoryName(budget.getCategory() != null ? budget.getCategory().getName() : "总预算")
                .year(budget.getYear())
                .month(budget.getMonth())
                .budgetAmount(budget.getBudgetAmount())
                .spentAmount(budget.getSpentAmount())
                .remaining(remaining)
                .usagePercent(usagePercent)
                .isOverBudget(budget.getIsOverBudget())
                .build();
    }
}
