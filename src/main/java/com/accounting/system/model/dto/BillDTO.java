package com.accounting.system.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BillDTO {
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotBlank(message = "类型不能为空")
    private String type;  // INCOME / EXPENSE

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    private String description;

    @NotNull(message = "账单日期不能为空")
    private LocalDate billDate;
}
