package de.safti.specs.io;

/**
 * Custom runtime exception for BitWriter errors, replacing checked IOExceptions.
 */
class BitStreamWriteException extends RuntimeException {
    public BitStreamWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}