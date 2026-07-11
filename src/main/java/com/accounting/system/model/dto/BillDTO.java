package com.accounting.system.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ============================================================
 * 账单请求DTO —— 前端提交账单数据的数据传输对象
 * ============================================================
 *
 * 【DTO vs Entity 的分层意义】
 * 为什么不让前端直接传Bill实体？
 * 1. 安全：防止客户端篡改不应修改的字段（如userId从Token中获取，不由前端传）
 * 2. 灵活：DTO字段可以和Entity不同（如type用String接收，Service层转枚举）
 * 3. 校验：DTO层用@Valid做参数校验，Entity层保持纯净
 *
 * 【Jakarta Validation校验注解】
 * @NotNull：字段不能为null（用于Long、BigDecimal、LocalDate等非字符串类型）
 * @NotBlank：字符串不能为null、空字符串或纯空格
 * @DecimalMin：数字最小值（金额必须大于0.01）
 */
@Data
public class BillDTO {
    /** 分类ID */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /** 收支类型：前端传"INCOME"或"EXPENSE"字符串，Service层转枚举 */
    @NotBlank(message = "类型不能为空")
    private String type;

    /** 金额：必须大于0.01元，金额类型用BigDecimal保证精度 */
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    /** 备注描述（可选） */
    private String description;

    /** 账单日期（实际发生的日期，不同于创建时间） */
    @NotNull(message = "账单日期不能为空")
    private LocalDate billDate;
}
