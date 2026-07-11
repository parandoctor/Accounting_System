package com.accounting.system.controller;

import com.accounting.system.model.dto.RecurringBillDTO;
import com.accounting.system.model.vo.RecurringBillVO;
import com.accounting.system.model.vo.ResultVO;
import com.accounting.system.security.UserPrincipal;
import com.accounting.system.service.RecurringBillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================
 * 周期账单控制器
 * ============================================================
 *
 * 【周期账单】
 * 自动定期生成账单，适合租金、订阅等固定支出。
 * 定时任务 processDueRecurringBills 由 @Scheduled 驱动，
 * 不通过 Controller 调用。
 *
 * 【删除机制】
 * 使用软删除（isActive=false），数据不丢失
 */
@RestController
@RequestMapping("/api/recurring-bills")
@RequiredArgsConstructor
public class RecurringBillController {

    private final RecurringBillService recurringBillService;

    /**
     * 创建周期账单
     */
    @PostMapping
    public ResultVO<RecurringBillVO> createRecurringBill(@AuthenticationPrincipal UserPrincipal principal,
                                                          @Valid @RequestBody RecurringBillDTO dto) {
        RecurringBillVO vo = recurringBillService.createRecurringBill(principal.getUserId(), dto);
        return ResultVO.success(vo);
    }

    /**
     * 查询当前用户所有活跃周期账单
     */
    @GetMapping
    public ResultVO<List<RecurringBillVO>> listRecurringBills(@AuthenticationPrincipal UserPrincipal principal) {
        List<RecurringBillVO> list = recurringBillService.listRecurringBills(principal.getUserId());
        return ResultVO.success(list);
    }

    /**
     * 软删除周期账单（设置 isActive=false）
     */
    @DeleteMapping("/{id}")
    public ResultVO<Void> deleteRecurringBill(@AuthenticationPrincipal UserPrincipal principal,
                                               @PathVariable Long id) {
        recurringBillService.deleteRecurringBill(principal.getUserId(), id);
        return ResultVO.success();
    }
}
