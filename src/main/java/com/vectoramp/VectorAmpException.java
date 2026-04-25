package com.vectoramp;

/** Base unchecked exception for VectorAmp SDK failures. */
public class VectorAmpException extends RuntimeException {
    public VectorAmpException(String message) {
        super(message);
    }

    public VectorAmpException(String message, Throwable cause) {
        super(message, cause);
    }
}
