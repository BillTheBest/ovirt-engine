package org.ovirt.engine.core.utils;

/**
 *
 */
public class SerializationExeption extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -5307210556111127692L;

    /**
     *
     */
    public SerializationExeption() {
    }

    /**
     * @param message
     */
    public SerializationExeption(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public SerializationExeption(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public SerializationExeption(String message, Throwable cause) {
        super(message, cause);
    }

}
