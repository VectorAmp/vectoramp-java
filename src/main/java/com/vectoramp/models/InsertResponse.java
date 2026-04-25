package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response returned after vector insertion. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InsertResponse {
    private int inserted;
    /**
     * @return inserted
     */
    public int getInserted() { return inserted; }
}
