package com.apiplatform.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 *
 * @param <T> 数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 数据列表 */
    private List<T> records;

    /** 总记录数 */
    private Long total;

    /** 当前页 */
    private Long current;

    /** 每页大小 */
    private Long size;

    /** 总页数 */
    private Long pages;

    /** 是否有上一页 */
    private Boolean hasPrevious;

    /** 是否有下一页 */
    private Boolean hasNext;

    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, Long total, Long current, Long size) {
        long pages = size > 0 ? (total + size - 1) / size : 0;
        return PageResult.<T>builder()
                .records(records)
                .total(total)
                .current(current)
                .size(size)
                .pages(pages)
                .hasPrevious(current > 1)
                .hasNext(current < pages)
                .build();
    }

    /**
     * 构建空分页结果
     */
    public static <T> PageResult<T> empty(Long current, Long size) {
        return of(List.of(), 0L, current, size);
    }
}
