package com.vectoramp.models;

import java.util.List;

/** Generic pagination envelope used by list endpoints. */
public class Page<T> {
    private final List<T> items;
    private final int total;
    private final int limit;
    private final int offset;

    public Page(List<T> items, int total, int limit, int offset) {
        this.items = items;
        this.total = total;
        this.limit = limit;
        this.offset = offset;
    }

    public List<T> getItems() { return items; }
    public int getTotal() { return total; }
    public int getLimit() { return limit; }
    public int getOffset() { return offset; }
}
