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

/**
 * ============================================================
 * 账单控制器 —— 系统最复杂的Controller
 * ============================================================
 *
 * 【核心职责】
 * 从 @AuthenticationPrincipal 获取当前用户ID，
 * 传递给Service层——这是数据隔离的第一道防线。
 *
 * 【数据隔离机制回顾】
 * Controller  ←  提取 userId from JWT Token
 *    ↓
 * Service     ←  所有查询/修改带 userId 条件
 *    ↓
 * Repository  ←  SQL WHERE user_id = ?
 *
 * 三层防护确保用户A绝不可能访问用户B的数据
 *
 * 【统计接口】
 * /statistics/category — 按分类聚合（饼图）
 * /statistics/time — 按时间范围聚合（折线图/柱状图）
 * /balance — 收支总览
 */
@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    /**
     * 创建账单
     * principal.getUserId() 从JWT中提取，不可伪造
     */
    @PostMapping
    public ResultVO<BillVO> createBill(@AuthenticationPrincipal UserPrincipal principal,
                                        @Valid @RequestBody BillDTO dto) {
        BillVO vo = billService.createBill(principal.getUserId(), dto);
        return ResultVO.success(vo);
    }

    /**
     * 更新账单（需校验归属）
     */
    @PutMapping("/{id}")
    public ResultVO<BillVO> updateBill(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id,
                                        @Valid @RequestBody BillDTO dto) {
        BillVO vo = billService.updateBill(principal.getUserId(), id, dto);
        return ResultVO.success(vo);
    }

    /**
     * 删除账单（软删除/物理删除由Service决定）
     */
    @DeleteMapping("/{id}")
    public ResultVO<Void> deleteBill(@AuthenticationPrincipal UserPrincipal principal,
                                      @PathVariable Long id) {
        billService.deleteBill(principal.getUserId(), id);
        return ResultVO.success();
    }

    /**
     * 分页查询账单 —— 支持多条件动态筛选
     * 将6个可选查询参数打包为 BillQueryDTO 传入Service
     */
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

    /**
     * 账单详情
     */
    @GetMapping("/{id}")
    public ResultVO<BillVO> getBillDetail(@AuthenticationPrincipal UserPrincipal principal,
                                           @PathVariable Long id) {
        BillVO vo = billService.getBillDetail(principal.getUserId(), id);
        return ResultVO.success(vo);
    }

    /**
     * 收支总览
     */
    @GetMapping("/balance")
    public ResultVO<BalanceVO> getBalance(@AuthenticationPrincipal UserPrincipal principal) {
        BalanceVO vo = billService.getBalance(principal.getUserId());
        return ResultVO.success(vo);
    }

    /**
     * 按分类统计 —— 用于饼图展示
     */
    @GetMapping("/statistics/category")
    public ResultVO<StatisticsVO> getStatisticsByCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        StatisticsVO vo = billService.getStatisticsByCategory(principal.getUserId(), startDate, endDate);
        return ResultVO.success(vo);
    }

    /**
     * 按时间范围统计
     */
    @GetMapping("/statistics/time")
    public ResultVO<StatisticsVO> getStatisticsByTimeRange(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        StatisticsVO vo = billService.getStatisticsByTimeRange(principal.getUserId(), startDate, endDate);
        return ResultVO.success(vo);
    }
}
