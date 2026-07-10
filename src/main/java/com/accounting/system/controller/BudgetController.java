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

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResultVO<BudgetVO> setBudget(@AuthenticationPrincipal UserPrincipal principal,
                                         @Valid @RequestBody BudgetDTO dto) {
        BudgetVO vo = budgetService.setBudget(principal.getUserId(), dto);
        return ResultVO.success(vo);
    }

    @GetMapping
    public ResultVO<List<BudgetVO>> getBudgets(@AuthenticationPrincipal UserPrincipal principal,
                                                @RequestParam int year,
                                                @RequestParam int month) {
        List<BudgetVO> list = budgetService.getBudgets(principal.getUserId(), year, month);
        return ResultVO.success(list);
    }

    @GetMapping("/usage")
    public ResultVO<List<BudgetVO>> getBudgetUsage(@AuthenticationPrincipal UserPrincipal principal,
                                                    @RequestParam int year,
                                                    @RequestParam int month) {
        List<BudgetVO> list = budgetService.getBudgetUsage(principal.getUserId(), year, month);
        return ResultVO.success(list);
    }
}
