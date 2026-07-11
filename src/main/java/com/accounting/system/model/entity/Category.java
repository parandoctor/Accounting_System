package com.accounting.system.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * ============================================================
 * 收支分类实体 —— 系统的基础字典数据
 * ============================================================
 *
 * 【设计定位】
 * Category是系统的"字典表"，存储所有收支分类。
 * 由 DataInitializer 在应用启动时初始化（18个分类），管理员也可扩展。
 *
 * 【分类设计原则】
 * - 支出分类覆盖日常生活场景（餐饮、交通、购物等）
 * - 收入分类覆盖常见收入来源（工资、奖金、投资等）
 * - 每组都有"其他"兜底（sortOrder=99排最末）
 * - emoji图标让前端渲染简单直观，无需额外图标库
 *
 * 【与Bill的关系】
 * Category ←(ManyToOne)— Bill：多条账单可属于同一分类
 * 删除分类时需检查是否有账单引用
 */
@Entity
@Table(name = "t_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 分类名称，如"餐饮"、"工资" */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /** 分类类型：INCOME(收入大类)/EXPENSE(支出大类) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CategoryType type;

    /** 图标：使用emoji字符，前端直接渲染 */
    @Column(length = 100)
    private String icon;

    /** 排序：数字越小越靠前，99为"其他"兜底 */
    @Column(name = "sort_order")
    private Integer sortOrder;

    public enum CategoryType {
        INCOME, EXPENSE
    }
}
