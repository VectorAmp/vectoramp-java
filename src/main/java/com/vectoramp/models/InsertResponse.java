package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InsertResponse {
    private int inserted;
    public int getInserted() { return inserted; }
}
