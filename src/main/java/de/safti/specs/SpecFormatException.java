package de.safti.specs;

public class SpecFormatException extends RuntimeException {
    public SpecFormatException(String message) {
        super(message);
    }

    public SpecFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpecFormatException(Throwable cause) {
        super(cause);
    }

    public SpecFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SpecFormatException() {
    }
}
