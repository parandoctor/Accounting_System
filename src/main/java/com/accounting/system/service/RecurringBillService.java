package com.accounting.system.service;

import com.accounting.system.model.dto.RecurringBillDTO;
import com.accounting.system.model.vo.RecurringBillVO;
import java.util.List;

public interface RecurringBillService {
    RecurringBillVO createRecurringBill(Long userId, RecurringBillDTO dto);
    List<RecurringBillVO> listRecurringBills(Long userId);
    void deleteRecurringBill(Long userId, Long id);
    void processDueRecurringBills();
}
