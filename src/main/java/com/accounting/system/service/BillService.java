package com.accounting.system.service;

import com.accounting.system.model.dto.*;
import com.accounting.system.model.vo.*;

import java.time.LocalDate;

/**
 * ============================================================
 * 账单服务接口 —— 系统最核心的业务逻辑
 * ============================================================
 *
 * 【功能范围】
 * 涵盖账单的完整生命周期：增删改查 + 余额统计 + 分类/时间统计
 * 
 * 【数据隔离原则】
 * 所有方法都需要传入 userId，确保用户只能操作自己的数据。
 * userId来自JWT Token（由Controller层通过@AuthenticationPrincipal获取并传入）
 */
public interface BillService {
    /** 创建账单 */
    BillVO createBill(Long userId, BillDTO dto);
    /** 更新账单（校验归属权） */
    BillVO updateBill(Long userId, Long billId, BillDTO dto);
    /** 删除账单（校验归属权） */
    void deleteBill(Long userId, Long billId);
    /** 分页查询账单（支持多条件筛选） */
    PageVO<BillVO> listBills(Long userId, BillQueryDTO query);
    /** 查看账单详情（校验归属权） */
    BillVO getBillDetail(Long userId, Long billId);
    /** 查询收支总览：总收入、总支出、结余 */
    BalanceVO getBalance(Long userId);
    /** 按分类统计（饼图数据） */
    StatisticsVO getStatisticsByCategory(Long userId, LocalDate startDate, LocalDate endDate);
    /** 按时间范围统计（趋势图数据） */
    StatisticsVO getStatisticsByTimeRange(Long userId, LocalDate startDate, LocalDate endDate);
}
