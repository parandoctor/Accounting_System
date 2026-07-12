package com.accounting.system.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ============================================================
 * 预算实体 —— 用于月度消费规划与超支预警
 * ============================================================
 *
 * 【业务场景】
 * 用户可以为自己设定每月的消费预算，系统自动追踪已花费金额，
 * 当支出超过预算时标记预警（isOverBudget = true）。
 *
 * 【唯一约束设计】
 * (user_id, year, month, category_id) 四字段联合唯一：
 *   一个用户在一个月内，对同一分类只能设置一条预算
 *   category_id 可为 null（表示总预算，不区分分类）
 *
 * 【预算追踪机制 v1.0.1】
 * spentAmount 和 isOverBudget 保留在实体中用于持久化存储，
 * 但查询时优先使用实时聚合计算（从 Bill 表 GROUP BY），
 * 保证数据始终准确。
 *
 * 【与Bill的区别】
 * Bill  —— 已发生的收支记录（事实）
 * Budget —— 未来支出的计划（规划）
 * 两者通过 spentAmount 字段关联：Budget.spentAmount 来自 Bill 的实时汇总
 */
@Entity
@Table(name = "t_budget", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "year", "month", "category_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 预算分类：null表示总预算，否则为特定分类预算 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /** 预算年份 */
    @Column(nullable = false)
    private Integer year;

    /** 预算月份(1-12) */
    @Column(nullable = false)
    private Integer month;

    /** 预算金额：计划花费的上限 */
    @Column(name = "budget_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal budgetAmount;

    /** 已花费金额：该分类当月实际支出总和，查询时优先实时聚合计算 */
    @Column(name = "spent_amount", precision = 12, scale = 2)
    private BigDecimal spentAmount;

    /** 是否超预算：spentAmount > budgetAmount 时自动设为true */
    @Column(name = "is_over_budget")
    private Boolean isOverBudget;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (spentAmount == null) spentAmount = BigDecimal.ZERO;
        if (isOverBudget == null) isOverBudget = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
