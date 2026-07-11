package com.accounting.system.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * ============================================================
 * 统计数据VO —— 收支统计的聚合视图
 * ============================================================
 *
 * 【用途】
 * 用于按分类统计、按时间统计两种场景，返回结构相同：
 *   - 头顶显示 totalIncome / totalExpense
 *   - 列表展示每个分类的金额和占比
 *
 * 【百分比计算】
 * percentage 是相对于同类总额的占比：
 *   - 某支出分类占比 = 该分类支出 / 总支出 * 100
 *   - 某收入分类占比 = 该分类收入 / 总收入 * 100
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsVO {
    /** 总收入（所有收入分类之和） */
    private BigDecimal totalIncome;
    /** 总支出（所有支出分类之和） */
    private BigDecimal totalExpense;
    /** 分类统计明细列表 */
    private List<CategoryStat> categories;

    /**
     * 分类统计明细
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStat {
        private Long categoryId;
        private String categoryName;
        private String categoryIcon;
        /** 收支类型：INCOME/EXPENSE */
        private String type;
        /** 该分类汇总金额 */
        private BigDecimal amount;
        /** 该分类在同类总额中的占比（百分比，如 35.5 表示35.5%） */
        private double percentage;
        /** 该分类账单笔数 */
        private long count;
    }
}
