package com.accounting.system.service;

import com.accounting.system.model.dto.BudgetDTO;
import com.accounting.system.model.vo.BudgetVO;
import java.util.List;

public interface BudgetService {
    BudgetVO setBudget(Long userId, BudgetDTO dto);
    List<BudgetVO> getBudgets(Long userId, int year, int month);
    List<BudgetVO> getBudgetUsage(Long userId, int year, int month);
}
