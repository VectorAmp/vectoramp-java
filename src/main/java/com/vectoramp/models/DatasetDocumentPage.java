package com.vectoramp.models;

import java.util.List;

/** Cursor-paginated dataset document page. Use nextCursor for the next request; do not infer offsets. */
public class DatasetDocumentPage {
    private final List<DatasetDocument> documents;
    private final String nextCursor;
    private final Integer limit;

    /**
     * Creates a dataset document page.
     * @param documents document metadata records
     * @param nextCursor cursor for the next page, or null when exhausted
     * @param limit effective page limit, when returned
     */
    public DatasetDocumentPage(List<DatasetDocument> documents, String nextCursor, Integer limit) {
        this.documents = documents;
        this.nextCursor = nextCursor;
        this.limit = limit;
    }

    /** @return document metadata records */
    public List<DatasetDocument> getDocuments() { return documents; }
    /** @return cursor for the next page, or null */
    public String getNextCursor() { return nextCursor; }
    /** @return effective page limit, when returned */
    public Integer getLimit() { return limit; }
}
