package com.vectoramp.models;

import java.util.List;

/** Generic pagination envelope used by list endpoints. */
public class Page<T> {
    private final List<T> items;
    private final int total;
    private final int limit;
    private final int offset;

    /**
     * Creates a pagination envelope.
     *
     * @param items page items
     * @param total total matching records
     * @param limit effective page limit
     * @param offset effective page offset
     */
    public Page(List<T> items, int total, int limit, int offset) {
        this.items = items;
        this.total = total;
        this.limit = limit;
        this.offset = offset;
    }

    /**
     * @return items
     */
    public List<T> getItems() { return items; }
    /**
     * @return total
     */
    public int getTotal() { return total; }
    /**
     * @return limit
     */
    public int getLimit() { return limit; }
    /**
     * @return offset
     */
    public int getOffset() { return offset; }
}
