package it.fulminazzo.railway

import org.jetbrains.annotations.NotNull

/**
 * Represents an exception thrown by this class.
 */
class RailwayException extends Exception {

    /**
     * Instantiates a new RailwayException
     *
     * @param message the message
     */
    RailwayException(@NotNull String message) {
        super(Objects.requireNonNull(message, 'Expected message to be not null'))
    }

}
