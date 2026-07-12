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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预算服务实现 —— 重构版
 * - 不再存储spentAmount/isOverBudget，完全实时计算
 * - 使用批量查询优化性能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final BillRepository billRepository;

    /**
     * 设置预算（创建或更新）
     * 只保存预算金额，不计算已花费
     */
    @Override
    @Transactional
    public BudgetVO setBudget(Long userId, BudgetDTO dto) {
        Budget budget;
        if (dto.getCategoryId() != null) {
            budget = budgetRepository.findByUserIdAndYearAndMonthAndCategoryId(
                    userId, dto.getYear(), dto.getMonth(), dto.getCategoryId())
                    .orElseGet(() -> {
                        Category category = categoryRepository.findById(dto.getCategoryId())
                                .orElseThrow(() -> new BusinessException("分类不存在"));
                        return Budget.builder()
                                .userId(userId)
                                .year(dto.getYear())
                                .month(dto.getMonth())
                                .category(category)
                                .budgetAmount(BigDecimal.ZERO)
                                .build();
                    });
        } else {
            budget = budgetRepository.findByUserIdAndYearAndMonthAndCategoryIsNull(
                    userId, dto.getYear(), dto.getMonth())
                    .orElseGet(() -> Budget.builder()
                            .userId(userId)
                            .year(dto.getYear())
                            .month(dto.getMonth())
                            .budgetAmount(BigDecimal.ZERO)
                            .build());
        }
        budget.setBudgetAmount(dto.getBudgetAmount());
        // 注意：不设置spentAmount和isOverBudget，留给查询时动态计算

        budget = budgetRepository.save(budget);
        log.info("Budget set for user {}: year={}, month={}, category={}, amount={}",
                userId, dto.getYear(), dto.getMonth(), dto.getCategoryId(), dto.getBudgetAmount());
        return buildBudgetVO(budget, BigDecimal.ZERO, false); // 占位，实际在get方法中重新计算
    }

    /**
     * 获取预算列表（含实时支出）
     */
    @Override
    public List<BudgetVO> getBudgets(Long userId, int year, int month) {
        List<Budget> budgets = budgetRepository.findByUserIdAndYearAndMonth(userId, year, month);
        // 批量获取该月各分类支出
        Map<Long, BigDecimal> spentMap = getMonthlySpentByCategory(userId, year, month);
        // 总支出（categoryId=null表示总支出）
        BigDecimal totalSpent = spentMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        return budgets.stream().map(budget -> {
            Long catId = budget.getCategory() != null ? budget.getCategory().getId() : null;
            BigDecimal spent = catId == null ? totalSpent : spentMap.getOrDefault(catId, BigDecimal.ZERO);
            boolean over = spent.compareTo(budget.getBudgetAmount()) > 0;
            return buildBudgetVO(budget, spent, over);
        }).collect(Collectors.toList());
    }

    /**
     * 获取预算使用率（与getBudgets逻辑相同，但可单独使用）
     */
    @Override
    public List<BudgetVO> getBudgetUsage(Long userId, int year, int month) {
        // 与getBudgets实现一致，可复用，但为了接口清晰，直接调用
        return getBudgets(userId, year, month);
    }

    /**
     * 批量查询用户某月各分类的支出总额（仅支出类型）
     */
    private Map<Long, BigDecimal> getMonthlySpentByCategory(Long userId, int year, int month) {
        List<Object[]> results = billRepository.sumExpenseGroupByCategoryForMonth(userId, year, month);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],   // categoryId
                        row -> (BigDecimal) row[1], // amount
                        BigDecimal::add
                ));
    }

    /**
     * 构建BudgetVO
     */
    private BudgetVO buildBudgetVO(Budget budget, BigDecimal spentAmount, boolean isOverBudget) {
        BigDecimal remaining = budget.getBudgetAmount().subtract(spentAmount);
        double usagePercent = budget.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0
                ? spentAmount.divide(budget.getBudgetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        return BudgetVO.builder()
                .id(budget.getId())
                .categoryId(budget.getCategory() != null ? budget.getCategory().getId() : null)
                .categoryName(budget.getCategory() != null ? budget.getCategory().getName() : "总预算")
                .year(budget.getYear())
                .month(budget.getMonth())
                .budgetAmount(budget.getBudgetAmount())
                .spentAmount(spentAmount)
                .remaining(remaining)
                .usagePercent(usagePercent)
                .isOverBudget(isOverBudget)
                .build();
    }
}