package com.accounting.system.model.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BillQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
    private String type;         // INCOME / EXPENSE
    private Long categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
}
