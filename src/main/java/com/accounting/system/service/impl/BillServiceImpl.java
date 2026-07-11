package com.accounting.system.service.impl;

import com.accounting.system.exception.BusinessException;
import com.accounting.system.model.dto.BillDTO;
import com.accounting.system.model.dto.BillQueryDTO;
import com.accounting.system.model.entity.Bill;
import com.accounting.system.model.entity.Category;
import com.accounting.system.model.vo.*;
import com.accounting.system.repository.BillRepository;
import com.accounting.system.repository.CategoryRepository;
import com.accounting.system.service.BillService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * 账单服务实现 —— 系统最复杂的业务逻辑
 * ============================================================
 *
 * 【数据安全设计】
 * 每个涉及具体账单的方法都遵循"先查后验"模式：
 * 1. 根据ID查询账单
 * 2. 校验 bill.getUserId().equals(userId) —— 防止水平越权
 * 3. 执行操作
 * 这是多用户系统的核心安全实践：永远不要信任前端传来的ID归属
 *
 * 【动态查询设计】
 * listBills使用了JPA Specification（动态条件查询）：
 * 用户可任意组合筛选条件（type/categoryId/dateRange/keyword）,
 * 不需要为每种组合写一个Repository方法。
 *
 * Specification本质是一个匿名函数，生成 WHERE 子句的 Predicate 列表,
 * 最后用 cb.and() 将所有条件AND连接。
 */
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 创建账单
     * 1. 校验分类存在
     * 2. 构建Bill实体（userId从参数获取，不由前端传递）
     * 3. 保存并返回VO
     */
    @Override
    @Transactional
    public BillVO createBill(Long userId, BillDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BusinessException("分类不存在"));

        Bill bill = Bill.builder()
                .userId(userId)
                .category(category)
                .type(Bill.BillType.valueOf(dto.getType().toUpperCase()))
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .billDate(dto.getBillDate())
                .build();

        bill = billRepository.save(bill);
        return toBillVO(bill);
    }

    /**
     * 修改账单
     * 
     * 安全校验：
     * 1. 账单必须存在
     * 2. 账单必须属于当前用户（防止水平越权：用户A修改用户B的账单）
     */
    @Override
    @Transactional
    public BillVO updateBill(Long userId, Long billId, BillDTO dto) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BusinessException("账单不存在"));

        // 数据归属校验 —— 防止水平越权的核心
        if (!bill.getUserId().equals(userId)) {
            throw new BusinessException("无权修改他人账单");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BusinessException("分类不存在"));

        bill.setCategory(category);
        bill.setType(Bill.BillType.valueOf(dto.getType().toUpperCase()));
        bill.setAmount(dto.getAmount());
        bill.setDescription(dto.getDescription());
        bill.setBillDate(dto.getBillDate());

        bill = billRepository.save(bill);
        return toBillVO(bill);
    }

    /**
     * 删除账单
     * 同样需要归属权校验
     */
    @Override
    @Transactional
    public void deleteBill(Long userId, Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BusinessException("账单不存在"));

        if (!bill.getUserId().equals(userId)) {
            throw new BusinessException("无权删除他人账单");
        }

        billRepository.delete(bill);
    }

    /**
     * 分页查询账单 —— 支持多条件动态筛选
     *
     * Specification 动态构建 WHERE 条件：
     * - 基础条件：userId（必须，数据隔离）
     * - 可选条件：type、categoryId、startDate、endDate、keyword
     * - 所有可选条件为null时自动跳过，实现"可选筛选"
     *
     * 排序：先按账单日期倒序，再按创建时间倒序（最新在前）
     */
    @Override
    public PageVO<BillVO> listBills(Long userId, BillQueryDTO query) {
        // 构建动态查询条件
        Specification<Bill> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 基础条件：必须属于当前用户
            predicates.add(cb.equal(root.get("userId"), userId));

            // 可选条件：按类型筛选
            if (query.getType() != null && !query.getType().isEmpty()) {
                predicates.add(cb.equal(root.get("type"), Bill.BillType.valueOf(query.getType().toUpperCase())));
            }
            // 可选条件：按分类筛选
            if (query.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), query.getCategoryId()));
            }
            // 可选条件：日期范围起始
            if (query.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("billDate"), query.getStartDate()));
            }
            // 可选条件：日期范围截止
            if (query.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("billDate"), query.getEndDate()));
            }
            // 可选条件：关键词模糊搜索（匹配描述字段）
            if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
                predicates.add(cb.like(root.get("description"), "%" + query.getKeyword() + "%"));
            }

            // 所有条件AND连接
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 分页参数：Spring Data 的 PageRequest 从0开始，而前端传1开始
        PageRequest pageRequest = PageRequest.of(
                query.getPage() - 1, query.getSize(),
                Sort.by(Sort.Direction.DESC, "billDate", "createdAt")
        );

        Page<Bill> page = billRepository.findAll(spec, pageRequest);

        List<BillVO> records = page.getContent().stream()
                .map(this::toBillVO)
                .collect(Collectors.toList());

        return PageVO.of(page.getTotalElements(), query.getPage(), query.getSize(), records);
    }

    /**
     * 查看账单详情（同样需要归属校验）
     */
    @Override
    public BillVO getBillDetail(Long userId, Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BusinessException("账单不存在"));

        if (!bill.getUserId().equals(userId)) {
            throw new BusinessException("无权查看他人账单");
        }

        return toBillVO(bill);
    }

    /**
     * 收支总览：统计该用户所有收入/支出/结余
     */
    @Override
    public BalanceVO getBalance(Long userId) {
        BigDecimal totalIncome = billRepository.sumAmountByUserIdAndType(userId, Bill.BillType.INCOME);
        BigDecimal totalExpense = billRepository.sumAmountByUserIdAndType(userId, Bill.BillType.EXPENSE);

        // COALESCE保证了数据库层不返回null，但这里做防御性编程
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        return BalanceVO.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(totalIncome.subtract(totalExpense))
                .build();
    }

    /**
     * 按分类统计 —— 用于饼图展示
     *
     * 核心逻辑：
     * 1. 查询分类汇总数据（分组聚合）
     * 2. 遍历结果，分别累加收入/支出总额
     * 3. 计算每个分类在同类中的占比
     *
     * 百分比算法：分类金额 / 同类总额 * 100（精确到小数点后2位）
     */
    @Override
    public StatisticsVO getStatisticsByCategory(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = billRepository.sumByCategory(userId, startDate, endDate);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        List<StatisticsVO.CategoryStat> categoryStats = new ArrayList<>();
        for (Object[] row : results) {
            Long categoryId = (Long) row[0];
            String categoryName = (String) row[1];
            String categoryIcon = (String) row[2];
            Bill.BillType type = (Bill.BillType) row[3];
            BigDecimal amount = (BigDecimal) row[4];
            long count = (Long) row[5];

            // 累加分类汇总
            if (type == Bill.BillType.INCOME) {
                totalIncome = totalIncome.add(amount);
            } else {
                totalExpense = totalExpense.add(amount);
            }

            categoryStats.add(StatisticsVO.CategoryStat.builder()
                    .categoryId(categoryId)
                    .categoryName(categoryName)
                    .categoryIcon(categoryIcon)
                    .type(type.name())
                    .amount(amount)
                    .count(count)
                    .build());
        }

        // 计算每个分类的百分比（相对于同类总额）
        for (StatisticsVO.CategoryStat stat : categoryStats) {
            BigDecimal total = stat.getType().equals("INCOME") ? totalIncome : totalExpense;
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                double pct = stat.getAmount().divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
                stat.setPercentage(pct);
            }
        }

        return StatisticsVO.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .categories(categoryStats)
                .build();
    }

    /**
     * 按时间范围统计 —— 复用按分类统计的逻辑
     * 时间范围已在查询条件中体现（startDate/endDate传入JPQL）
     */
    @Override
    public StatisticsVO getStatisticsByTimeRange(Long userId, LocalDate startDate, LocalDate endDate) {
        // Reuse the same logic - time-range filtered category statistics
        return getStatisticsByCategory(userId, startDate, endDate);
    }

    /**
     * Entity → VO 转换
     *
     * 注意：category字段在LAZY加载下，只有调用 category.getName() 时才会发SQL查询
     * 这是Hibernate的延迟加载机制，避免不必要的JOIN查询
     */
    private BillVO toBillVO(Bill bill) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return BillVO.builder()
                .id(bill.getId())
                .userId(bill.getUserId())
                .categoryId(bill.getCategory().getId())
                .categoryName(bill.getCategory().getName())
                .categoryIcon(bill.getCategory().getIcon())
                .type(bill.getType().name())
                .amount(bill.getAmount())
                .description(bill.getDescription())
                .billDate(bill.getBillDate())
                .createdAt(bill.getCreatedAt().format(fmt))
                .updatedAt(bill.getUpdatedAt().format(fmt))
                .build();
    }
}
