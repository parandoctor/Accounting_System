package com.accounting.system.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {
    private long total;
    private int page;
    private int size;
    private int totalPages;
    private List<T> records;

    public static <T> PageVO<T> of(long total, int page, int size, List<T> records) {
        return PageVO.<T>builder()
                .total(total)
                .page(page)
                .size(size)
                .totalPages((int) Math.ceil((double) total / size))
                .records(records)
                .build();
    }
}
