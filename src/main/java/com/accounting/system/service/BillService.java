package com.accounting.system.service;

import com.accounting.system.model.dto.*;
import com.accounting.system.model.vo.*;

import java.time.LocalDate;

public interface BillService {
    BillVO createBill(Long userId, BillDTO dto);
    BillVO updateBill(Long userId, Long billId, BillDTO dto);
    void deleteBill(Long userId, Long billId);
    PageVO<BillVO> listBills(Long userId, BillQueryDTO query);
    BillVO getBillDetail(Long userId, Long billId);
    BalanceVO getBalance(Long userId);
    StatisticsVO getStatisticsByCategory(Long userId, LocalDate startDate, LocalDate endDate);
    StatisticsVO getStatisticsByTimeRange(Long userId, LocalDate startDate, LocalDate endDate);
}
