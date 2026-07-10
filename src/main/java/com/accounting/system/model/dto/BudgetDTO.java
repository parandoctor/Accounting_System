package com.accounting.system.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetDTO {
    private Long categoryId;  // null = 总预算

    @NotNull(message = "年份不能为空")
    private Integer year;

    @NotNull(message = "月份不能为空")
    private Integer month;

    @NotNull(message = "预算金额不能为空")
    @DecimalMin(value = "0.01")
    private BigDecimal budgetAmount;
}
