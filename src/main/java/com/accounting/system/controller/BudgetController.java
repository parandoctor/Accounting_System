package com.accounting.system.controller;

import com.accounting.system.model.dto.BudgetDTO;
import com.accounting.system.model.vo.BudgetVO;
import com.accounting.system.model.vo.ResultVO;
import com.accounting.system.security.UserPrincipal;
import com.accounting.system.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================
 * 预算控制器
 * ============================================================
 *
 * 【预算功能】
 * 支持两种粒度的预算：
 * - 总预算：不传 categoryId
 * - 分类预算：传 categoryId
 *
 * /usage 接口返回预算使用率，用于前端进度条展示
 */
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * 设置/更新预算（Upsert）
     */
    @PostMapping
    public ResultVO<BudgetVO> setBudget(@AuthenticationPrincipal UserPrincipal principal,
                                         @Valid @RequestBody BudgetDTO dto) {
        BudgetVO vo = budgetService.setBudget(principal.getUserId(), dto);
        return ResultVO.success(vo);
    }

    /**
     * 获取预算列表（按年月查询）
     */
    @GetMapping
    public ResultVO<List<BudgetVO>> getBudgets(@AuthenticationPrincipal UserPrincipal principal,
                                                @RequestParam int year,
                                                @RequestParam int month) {
        List<BudgetVO> list = budgetService.getBudgets(principal.getUserId(), year, month);
        return ResultVO.success(list);
    }

    /**
     * 获取预算使用率 —— 前端进度条数据源
     */
    @GetMapping("/usage")
    public ResultVO<List<BudgetVO>> getBudgetUsage(@AuthenticationPrincipal UserPrincipal principal,
                                                    @RequestParam int year,
                                                    @RequestParam int month) {
        List<BudgetVO> list = budgetService.getBudgetUsage(principal.getUserId(), year, month);
        return ResultVO.success(list);
    }
}
