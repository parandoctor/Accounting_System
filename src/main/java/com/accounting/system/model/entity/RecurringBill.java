package com.accounting.system.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ============================================================
 * 周期性账单实体 —— 自动化的定期收支记录
 * ============================================================
 *
 * 【业务场景】
 * 很多收支是周期性重复的，如：
 * - 每月15号发工资（MONTHLY）
 * - 每周五聚餐（WEEKLY）
 * - 每年生日礼物（YEARLY）
 * - 每天地铁通勤（DAILY）
 *
 * 用户创建周期账单后，系统每天凌晨3点自动检查到期记录，
 * 自动生成对应的Bill记录，并计算下一次到期日。
 *
 * 【周期计算机制】
 * CycleType + CycleValue + nextDate 三者协作：
 *   DAILY + 1   → 每天一次，nextDate每天+1
 *   WEEKLY + 1  → 每周一次，nextDate每周+7天
 *   MONTHLY + 1 → 每月一次，nextDate每月+1月
 *   YEARLY + 1  → 每年一次，nextDate每年+1年
 *   WEEKLY + 2  → 每两周一次（cycleValue可自定义间隔）
 *
 * 【调度流程】
 * @Scheduled(cron="0 0 3 * * ?")  每天凌晨3点
 *   → 查询 isActive=true AND nextDate <= 今天 的记录
 *   → 为每条记录创建Bill
 *   → 计算下一个nextDate
 *
 * 【软删除设计】
 * isActive = false 表示逻辑删除：
 *   保留历史数据但不再生成新账单
 *   比物理删除更好：可追溯、可恢复
 */
@Entity
@Table(name = "t_recurring_bill")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** 收支类型：INCOME/EXPENSE */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Bill.BillType type;

    /** 每次的金额 */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** 备注说明 */
    @Column(length = 255)
    private String description;

    /** 周期类型：DAILY/WEEKLY/MONTHLY/YEARLY */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CycleType cycleType;

    /** 周期间隔值：如cycleType=WEEKLY + cycleValue=2 表示每两周 */
    @Column(name = "cycle_value")
    private Integer cycleValue;

    /** 下一次生成账单的日期 */
    @Column(name = "next_date", nullable = false)
    private LocalDate nextDate;

    /** 是否启用：false=软删除，不再自动生成 */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum CycleType {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
    }
}
