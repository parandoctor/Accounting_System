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

@RestController
@RequestMapping("/api/recurring-bills")
@RequiredArgsConstructor
public class RecurringBillController {

    private final RecurringBillService recurringBillService;

    @PostMapping
    public ResultVO<RecurringBillVO> createRecurringBill(@AuthenticationPrincipal UserPrincipal principal,
                                                          @Valid @RequestBody RecurringBillDTO dto) {
        RecurringBillVO vo = recurringBillService.createRecurringBill(principal.getUserId(), dto);
        return ResultVO.success(vo);
    }

    @GetMapping
    public ResultVO<List<RecurringBillVO>> listRecurringBills(@AuthenticationPrincipal UserPrincipal principal) {
        List<RecurringBillVO> list = recurringBillService.listRecurringBills(principal.getUserId());
        return ResultVO.success(list);
    }

    @DeleteMapping("/{id}")
    public ResultVO<Void> deleteRecurringBill(@AuthenticationPrincipal UserPrincipal principal,
                                               @PathVariable Long id) {
        recurringBillService.deleteRecurringBill(principal.getUserId(), id);
        return ResultVO.success();
    }
}
