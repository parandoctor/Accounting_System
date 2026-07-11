package com.accounting.system.model.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * ============================================================
 * 账单查询DTO —— 封装多条件筛选参数
 * ============================================================
 *
 * 【设计考量】
 * 为什么不用多个@RequestParam而封装成DTO？
 * - 当查询条件超过3个时，Controller方法签名会很长
 * - DTO可以在Service层间传递，比多个独立参数更清晰
 * - 方便后续扩展：加一个新条件只需加字段，不需要改方法签名
 *
 * 【默认值设计】
 * page=1, size=10：适配前端首次加载，无需传分页参数
 */
@Data
public class BillQueryDTO {
    /** 页码（1-based），默认第1页 */
    private Integer page = 1;
    /** 每页条数，默认10条 */
    private Integer size = 10;
    /** 收支类型筛选：INCOME/EXPENSE，null表示不限 */
    private String type;
    /** 分类筛选，null表示不限 */
    private Long categoryId;
    /** 开始日期，null表示不限 */
    private LocalDate startDate;
    /** 结束日期，null表示不限 */
    private LocalDate endDate;
    /** 关键词搜索（模糊匹配描述字段），null表示不限 */
    private String keyword;
}
