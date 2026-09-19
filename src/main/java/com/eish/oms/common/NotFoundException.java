package com.eish.oms.common;

/**
 * The requested resource does not exist, or the caller is not allowed to know that it exists.
 * Mapped to HTTP 404.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    /** Convenience factory: "Product 42 not found". */
    public static NotFoundException of(String resource, Object id) {
        return new NotFoundException(resource + " " + id + " not found");
    }
}
