package com.accounting.system.controller;

import com.accounting.system.model.dto.BillDTO;
import com.accounting.system.model.dto.BillQueryDTO;
import com.accounting.system.model.vo.*;
import com.accounting.system.security.UserPrincipal;
import com.accounting.system.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    public ResultVO<BillVO> createBill(@AuthenticationPrincipal UserPrincipal principal,
                                        @Valid @RequestBody BillDTO dto) {
        BillVO vo = billService.createBill(principal.getUserId(), dto);
        return ResultVO.success(vo);
    }

    @PutMapping("/{id}")
    public ResultVO<BillVO> updateBill(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id,
                                        @Valid @RequestBody BillDTO dto) {
        BillVO vo = billService.updateBill(principal.getUserId(), id, dto);
        return ResultVO.success(vo);
    }

    @DeleteMapping("/{id}")
    public ResultVO<Void> deleteBill(@AuthenticationPrincipal UserPrincipal principal,
                                      @PathVariable Long id) {
        billService.deleteBill(principal.getUserId(), id);
        return ResultVO.success();
    }

    @GetMapping
    public ResultVO<PageVO<BillVO>> listBills(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               @RequestParam(required = false) String type,
                                               @RequestParam(required = false) Long categoryId,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                               @RequestParam(required = false) String keyword) {
        BillQueryDTO query = new BillQueryDTO();
        query.setPage(page);
        query.setSize(size);
        query.setType(type);
        query.setCategoryId(categoryId);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setKeyword(keyword);

        PageVO<BillVO> pageVO = billService.listBills(principal.getUserId(), query);
        return ResultVO.success(pageVO);
    }

    @GetMapping("/{id}")
    public ResultVO<BillVO> getBillDetail(@AuthenticationPrincipal UserPrincipal principal,
                                           @PathVariable Long id) {
        BillVO vo = billService.getBillDetail(principal.getUserId(), id);
        return ResultVO.success(vo);
    }

    @GetMapping("/balance")
    public ResultVO<BalanceVO> getBalance(@AuthenticationPrincipal UserPrincipal principal) {
        BalanceVO vo = billService.getBalance(principal.getUserId());
        return ResultVO.success(vo);
    }

    @GetMapping("/statistics/category")
    public ResultVO<StatisticsVO> getStatisticsByCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        StatisticsVO vo = billService.getStatisticsByCategory(principal.getUserId(), startDate, endDate);
        return ResultVO.success(vo);
    }

    @GetMapping("/statistics/time")
    public ResultVO<StatisticsVO> getStatisticsByTimeRange(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        StatisticsVO vo = billService.getStatisticsByTimeRange(principal.getUserId(), startDate, endDate);
        return ResultVO.success(vo);
    }
}
