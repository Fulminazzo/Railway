package it.fulminazzo.railway

import org.jetbrains.annotations.NotNull

/**
 * Represents an exception thrown in {@link ContentHandler}
 */
class ContentHandlerException extends Exception {

    /**
     * Instantiates a new ContentHandlerException
     *
     * @param message the message
     */
    ContentHandlerException(String message) {
        super(message)
    }

    /**
     * Instantiates a new ContentHandlerException
     *
     * @param message the message
     * @param cause the error that caused the exception
     */
    ContentHandlerException(String message, @NotNull Throwable cause) {
        super(message, Objects.requireNonNull(cause, "Expected cause to be not null"))
    }

}
