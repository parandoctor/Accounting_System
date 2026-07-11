package com.accounting.system.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * ============================================================
 * 分页响应VO —— 统一分页数据格式
 * ============================================================
 *
 * 【分页字段说明】
 * - total：总记录数（前端用于显示"共XX条"）
 * - page：当前页码（从1开始，而非0）
 * - size：每页条数
 * - totalPages：总页数 = ceil(total / size)，前端用于生成分页器
 * - records：当前页的数据列表
 *
 * 【分页计算要点】
 * 页码从1开始（面向用户），但Spring Data的PageRequest从0开始：
 *   Controller接收 page=1 → Service中转为 pageRequest.of(0, size)
 *   PageVO响应 page=1 → 前端直接使用
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {
    /** 总记录数 */
    private long total;
    /** 当前页码（1-based） */
    private int page;
    /** 每页条数 */
    private int size;
    /** 总页数 */
    private int totalPages;
    /** 当前页数据 */
    private List<T> records;

    /**
     * 工厂方法：将Spring Data的Page对象转为前端友好的PageVO
     *
     * @param total   总记录数
     * @param page    当前页码（将原样返回）
     * @param size    每页条数
     * @param records 当前页数据
     */
    public static <T> PageVO<T> of(long total, int page, int size, List<T> records) {
        return PageVO.<T>builder()
                .total(total)
                .page(page)
                .size(size)
                .totalPages((int) Math.ceil((double) total / size))  // 向上取整计算总页数
                .records(records)
                .build();
    }
}
