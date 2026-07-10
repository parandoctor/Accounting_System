package com.accounting.system.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecurringBillDTO {
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotNull(message = "类型不能为空")
    private String type;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private String description;

    @NotNull(message = "周期类型不能为空")
    private String cycleType;  // DAILY, WEEKLY, MONTHLY, YEARLY

    private Integer cycleValue;

    @NotNull(message = "下次执行日期不能为空")
    private LocalDate nextDate;
}
