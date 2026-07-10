package com.accounting.system.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringBillVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String type;
    private BigDecimal amount;
    private String description;
    private String cycleType;
    private Integer cycleValue;
    private LocalDate nextDate;
    private Boolean isActive;
}
