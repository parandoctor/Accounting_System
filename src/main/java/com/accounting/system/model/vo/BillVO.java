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
public class BillVO {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String type;
    private BigDecimal amount;
    private String description;
    private LocalDate billDate;
    private String createdAt;
    private String updatedAt;
}
