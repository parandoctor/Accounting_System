package com.accounting.system.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ============================================================
 * 账单响应VO —— 返回给前端的账单数据视图
 * ============================================================
 *
 * 【VO vs Entity 的设计考量】
 * BillVO 相比 Bill Entity 的差异：
 * 1. 展开关联：categoryId + categoryName + categoryIcon 三者平铺
 *    前端无需二次查询分类名称
 * 2. 类型扁平化：type 用 String 而非枚举，前端更友好
 * 3. 日期格式化：createdAt/updatedAt 用 String 而非 LocalDateTime
 *    由Service层格式化后赋值，前端直接展示
 * 4. 最小权限：不暴露 createdAt/updatedAt 以外的内部字段
 *
 * 【数据组装流程】
 * Bill Entity → BillServiceImpl.toBillVO() → BillVO → Controller → JSON 响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillVO {
    private Long id;
    private Long userId;
    /** 分类ID：前端跳转分类详情用 */
    private Long categoryId;
    /** 分类名称：直接展示 */
    private String categoryName;
    /** 分类图标：emoji展示 */
    private String categoryIcon;
    /** 收支类型：INCOME/EXPENSE */
    private String type;
    /** 金额 */
    private BigDecimal amount;
    /** 备注 */
    private String description;
    /** 账单日期 */
    private LocalDate billDate;
    /** 创建时间（已格式化为字符串） */
    private String createdAt;
    /** 更新时间（已格式化为字符串） */
    private String updatedAt;
}
