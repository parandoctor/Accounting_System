package com.accounting.system.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsVO {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private List<CategoryStat> categories;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStat {
        private Long categoryId;
        private String categoryName;
        private String categoryIcon;
        private String type;
        private BigDecimal amount;
        private double percentage;
        private long count;
    }
}
