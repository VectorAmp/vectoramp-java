package com.vectoramp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response returned after deleting vectors from a dataset. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeleteVectorsResponse {
    private int deleted;
    private String datasetId;

    /** @return number of vectors deleted */
    public int getDeleted() { return deleted; }
    /** @return dataset id */
    public String getDatasetId() { return datasetId; }
}
