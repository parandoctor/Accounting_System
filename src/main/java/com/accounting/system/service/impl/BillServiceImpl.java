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
import lombok.extern.slf4j.Slf4j;
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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final CategoryRepository categoryRepository;

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
        log.info("Bill created: user={}, amount={}, category={}", userId, dto.getAmount(), dto.getCategoryId());
        return toBillVO(bill);
    }

    @Override
    @Transactional
    public BillVO updateBill(Long userId, Long billId, BillDTO dto) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BusinessException("账单不存在"));

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
        log.info("Bill updated: user={}, billId={}", userId, billId);
        return toBillVO(bill);
    }

    @Override
    @Transactional
    public void deleteBill(Long userId, Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BusinessException("账单不存在"));

        if (!bill.getUserId().equals(userId)) {
            throw new BusinessException("无权删除他人账单");
        }

        billRepository.delete(bill);
        log.info("Bill deleted: user={}, billId={}", userId, billId);
    }

    @Override
    public PageVO<BillVO> listBills(Long userId, BillQueryDTO query) {
        Specification<Bill> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));

            if (query.getType() != null && !query.getType().isEmpty()) {
                predicates.add(cb.equal(root.get("type"), Bill.BillType.valueOf(query.getType().toUpperCase())));
            }
            if (query.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), query.getCategoryId()));
            }
            if (query.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("billDate"), query.getStartDate()));
            }
            if (query.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("billDate"), query.getEndDate()));
            }
            if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
                predicates.add(cb.like(root.get("description"), "%" + query.getKeyword() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

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

    @Override
    public BillVO getBillDetail(Long userId, Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BusinessException("账单不存在"));

        if (!bill.getUserId().equals(userId)) {
            throw new BusinessException("无权查看他人账单");
        }

        return toBillVO(bill);
    }

    @Override
    public BalanceVO getBalance(Long userId) {
        BigDecimal totalIncome = billRepository.sumAmountByUserIdAndType(userId, Bill.BillType.INCOME);
        BigDecimal totalExpense = billRepository.sumAmountByUserIdAndType(userId, Bill.BillType.EXPENSE);

        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        return BalanceVO.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(totalIncome.subtract(totalExpense))
                .build();
    }

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

    @Override
    public StatisticsVO getStatisticsByTimeRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return getStatisticsByCategory(userId, startDate, endDate);
    }

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