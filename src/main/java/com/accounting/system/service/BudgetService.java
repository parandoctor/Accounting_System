package com.accounting.system.service;

import com.accounting.system.model.dto.BudgetDTO;
import com.accounting.system.model.vo.BudgetVO;

import java.util.List;

/**
 * ============================================================
 * 预算服务接口
 * ============================================================
 *
 * 【预算功能】
 * 预算用于控制每月支出的上限，支持两种粒度：
 * 1. 总预算（不指定分类）：控制整月总支出
 * 2. 分类预算（指定categoryId）：控制某一分类的支出
 *
 * 【预算使用率】
 * getBudgetUsage 返回每个预算的已花费/预算金额比例，
 * 前端可用于展示进度条：绿色=正常，红色=超预算
 */
public interface BudgetService {
    BudgetVO setBudget(Long userId, BudgetDTO dto);
    List<BudgetVO> getBudgets(Long userId, int year, int month);
    List<BudgetVO> getBudgetUsage(Long userId, int year, int month);
}
