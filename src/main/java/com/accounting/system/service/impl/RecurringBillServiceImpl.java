package com.accounting.system.service.impl;

import com.accounting.system.exception.BusinessException;
import com.accounting.system.model.dto.RecurringBillDTO;
import com.accounting.system.model.entity.Bill;
import com.accounting.system.model.entity.Category;
import com.accounting.system.model.entity.RecurringBill;
import com.accounting.system.model.vo.RecurringBillVO;
import com.accounting.system.repository.BillRepository;
import com.accounting.system.repository.CategoryRepository;
import com.accounting.system.repository.RecurringBillRepository;
import com.accounting.system.service.RecurringBillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringBillServiceImpl implements RecurringBillService {

    private final RecurringBillRepository recurringBillRepository;
    private final CategoryRepository categoryRepository;
    private final BillRepository billRepository;

    @Override
    @Transactional
    public RecurringBillVO createRecurringBill(Long userId, RecurringBillDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BusinessException("分类不存在"));

        RecurringBill rb = RecurringBill.builder()
                .userId(userId)
                .category(category)
                .type(Bill.BillType.valueOf(dto.getType().toUpperCase()))
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .cycleType(RecurringBill.CycleType.valueOf(dto.getCycleType().toUpperCase()))
                .cycleValue(dto.getCycleValue() != null ? dto.getCycleValue() : 1)
                .nextDate(dto.getNextDate())
                .isActive(true)
                .build();

        rb = recurringBillRepository.save(rb);
        log.info("Recurring bill created for user {}: id={}, cycle={}", userId, rb.getId(), rb.getCycleType());
        return toRecurringBillVO(rb);
    }

    @Override
    public List<RecurringBillVO> listRecurringBills(Long userId) {
        return recurringBillRepository.findByUserIdAndIsActiveTrue(userId)
                .stream().map(this::toRecurringBillVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRecurringBill(Long userId, Long id) {
        RecurringBill rb = recurringBillRepository.findById(id)
                .orElseThrow(() -> new BusinessException("周期账单不存在"));
        if (!rb.getUserId().equals(userId)) {
            throw new BusinessException("无权操作他人周期账单");
        }
        rb.setIsActive(false);
        recurringBillRepository.save(rb);
        log.info("Recurring bill {} soft-deleted by user {}", id, userId);
    }

    /**
     * 定时任务：处理到期周期账单
     * 增加异常捕获，确保单条失败不影响整体
     */
    @Override
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void processDueRecurringBills() {
        List<RecurringBill> dueBills = recurringBillRepository
                .findByIsActiveTrueAndNextDateLessThanEqual(LocalDate.now());

        log.info("Found {} due recurring bills to process", dueBills.size());

        for (RecurringBill rb : dueBills) {
            try {
                // 创建实际账单
                Bill bill = Bill.builder()
                        .userId(rb.getUserId())
                        .category(rb.getCategory())
                        .type(rb.getType())
                        .amount(rb.getAmount())
                        .description("[周期] " + (rb.getDescription() != null ? rb.getDescription() : ""))
                        .billDate(LocalDate.now())
                        .build();
                billRepository.save(bill);

                // 计算下次日期
                rb.setNextDate(calculateNextDate(rb));
                recurringBillRepository.save(rb);

                log.debug("Generated bill for user {} from recurring bill {}", rb.getUserId(), rb.getId());
            } catch (Exception e) {
                log.error("Failed to process recurring bill id={} for user {}: {}",
                        rb.getId(), rb.getUserId(), e.getMessage(), e);
                // 继续处理下一条，不中断整个事务
            }
        }
    }

    private LocalDate calculateNextDate(RecurringBill rb) {
        int value = rb.getCycleValue() != null ? rb.getCycleValue() : 1;
        return switch (rb.getCycleType()) {
            case DAILY -> rb.getNextDate().plusDays(value);
            case WEEKLY -> rb.getNextDate().plusWeeks(value);
            case MONTHLY -> rb.getNextDate().plusMonths(value);
            case YEARLY -> rb.getNextDate().plusYears(value);
        };
    }

    private RecurringBillVO toRecurringBillVO(RecurringBill rb) {
        return RecurringBillVO.builder()
                .id(rb.getId())
                .categoryId(rb.getCategory().getId())
                .categoryName(rb.getCategory().getName())
                .type(rb.getType().name())
                .amount(rb.getAmount())
                .description(rb.getDescription())
                .cycleType(rb.getCycleType().name())
                .cycleValue(rb.getCycleValue())
                .nextDate(rb.getNextDate())
                .isActive(rb.getIsActive())
                .build();
    }
}