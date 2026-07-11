package com.accounting.system.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ============================================================
 * 账单实体 —— 系统的核心业务数据
 * ============================================================
 *
 * 【数据库索引设计】
 * 表上建立了4个索引，每个都有明确的查询场景：
 *   idx_bill_user_id   → WHERE user_id = ?  （用户查询自己的账单，最高频）
 *   idx_bill_date      → WHERE bill_date BETWEEN ? AND ? （按日期范围统计）
 *   idx_bill_category  → JOIN + WHERE category_id = ? （按分类筛选/统计）
 *   idx_bill_type      → WHERE type = ? （收支类型筛选）
 *
 * 【金额字段设计】
 * 使用 BigDecimal 而非 float/double：
 *   - float/double 是二进制浮点数，存在精度丢失（0.1+0.2≠0.3）
 *   - BigDecimal 是十进制精确计算，适合金额（precision=12, scale=2 表示最多12位其中2位小数）
 *
 * 【多对一关系】
 * Bill 与 Category 是 @ManyToOne 关系（多条账单属于同一分类）
 * FetchType.LAZY 延迟加载：只有访问 category.getName() 时才查询分类表
 * 避免每次查账单都连表查分类，提升性能
 * 
 * 【数据归属与安全】
 * userId 字段用于标识账单归属，是数据隔离的核心：
 *   - 查询时强制加 userId 条件，防止用户看到他人数据
 *   - 更新/删除时校验 userId 匹配，防止越权操作
 */
@Entity
@Table(name = "t_bill", indexes = {
    @Index(name = "idx_bill_user_id", columnList = "user_id"),
    @Index(name = "idx_bill_date", columnList = "bill_date"),
    @Index(name = "idx_bill_category", columnList = "category_id"),
    @Index(name = "idx_bill_type", columnList = "type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 数据归属：哪个用户的账单（数据隔离关键字段） */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 所属分类：关联Category表，LAZY延迟加载 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** 收支类型：INCOME(收入)/EXPENSE(支出) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BillType type;

    /** 金额：BigDecimal保证精度 */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** 备注：可选，描述这笔账单的详情 */
    @Column(length = 255)
    private String description;

    /** 账单日期：实际发生的日期，区别于创建时间 */
    @Column(name = "bill_date", nullable = false)
    private LocalDate billDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum BillType {
        INCOME, EXPENSE
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
