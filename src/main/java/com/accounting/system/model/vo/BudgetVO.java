package com.accounting.system.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private Integer year;
    private Integer month;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private BigDecimal remaining;
    private double usagePercent;
    private Boolean isOverBudget;
}
