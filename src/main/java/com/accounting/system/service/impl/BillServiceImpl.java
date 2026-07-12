package com.accounting.system.service.impl;

// 导入省略...
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {
    // ... 原有代码不变，只需在关键方法增加日志，例如：

    @Override
    @Transactional
    public BillVO createBill(Long userId, BillDTO dto) {
        // ... 原有逻辑 ...
        log.info("Bill created: user={}, amount={}, category={}", userId, dto.getAmount(), dto.getCategoryId());
        return toBillVO(bill);
    }

    @Override
    @Transactional
    public void deleteBill(Long userId, Long billId) {
        // ... 原有逻辑 ...
        log.info("Bill deleted: user={}, billId={}", userId, billId);
    }
    // 其他方法可类似添加日志
}