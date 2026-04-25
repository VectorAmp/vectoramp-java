package com.vectoramp;

/** Exception raised when the VectorAmp API returns a non-successful HTTP status. */
public class VectorAmpApiException extends VectorAmpException {
    private final int statusCode;
    private final String responseBody;

    public VectorAmpApiException(int statusCode, String responseBody) {
        super("VectorAmp API request failed with status " + statusCode + (responseBody == null || responseBody.isEmpty() ? "" : ": " + responseBody));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
