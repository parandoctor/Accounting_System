package com.accounting.system.service;

import com.accounting.system.model.dto.RecurringBillDTO;
import com.accounting.system.model.vo.RecurringBillVO;

import java.util.List;

/**
 * ============================================================
 * 周期账单服务接口
 * ============================================================
 *
 * 【周期账单】
 * 自动在指定周期（每天/每周/每月/每年）生成账单，
 * 适合租金、订阅服务等固定周期的收支记录
 *
 * 【定时处理】
 * processDueRecurringBills 由 @Scheduled 驱动，
 * 每天凌晨3点扫描所有到期未处理的周期账单，自动生成实际账单
 */
public interface RecurringBillService {
    RecurringBillVO createRecurringBill(Long userId, RecurringBillDTO dto);
    List<RecurringBillVO> listRecurringBills(Long userId);
    void deleteRecurringBill(Long userId, Long id);
    void processDueRecurringBills();
}
