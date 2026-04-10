package com.chongstack.ddd.types;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 分页结果封装。
 *
 * @param <T> 元素类型
 */
public class Page<T> {

    private final List<T> items;
    private final long totalCount;
    private final int pageIndex;
    private final int pageSize;

    private Page(List<T> items, long totalCount, int pageIndex, int pageSize) {
        this.items = items != null ? items : Collections.emptyList();
        this.totalCount = totalCount;
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    public static <T> Page<T> of(List<T> items, long totalCount, int pageIndex, int pageSize) {
        return new Page<>(items, totalCount, pageIndex, pageSize);
    }

    public static <T> Page<T> empty(int pageIndex, int pageSize) {
        return new Page<>(Collections.emptyList(), 0, pageIndex, pageSize);
    }

    public <R> Page<R> map(Function<T, R> mapper) {
        List<R> mapped = items.stream().map(mapper).toList();
        return new Page<>(mapped, totalCount, pageIndex, pageSize);
    }

    public List<T> getItems() {
        return Collections.unmodifiableList(items);
    }

    public long getTotalCount() {
        return totalCount;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalPages() {
        if (pageSize <= 0) return 0;
        return (totalCount + pageSize - 1) / pageSize;
    }

    public boolean hasNext() {
        return (long) pageIndex * pageSize + items.size() < totalCount;
    }
}
