package com.accounting.system.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * ============================================================
 * 收支余额VO —— 收入/支出/结余 汇总视图
 * ============================================================
 *
 * balance = totalIncome - totalExpense
 * 正数表示盈余，负数表示赤字
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceVO {
    /** 总收入 */
    private BigDecimal totalIncome;
    /** 总支出 */
    private BigDecimal totalExpense;
    /** 结余（收入 - 支出） */
    private BigDecimal balance;
}
