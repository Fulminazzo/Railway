package it.fulminazzo.railway

import org.jetbrains.annotations.NotNull

/**
 * The main root class.
 */
class Railway {
    static final DEFAULT_PORT = 80

    // <port> <rootDir>
    static void main(String[] args) {
        def port = DEFAULT_PORT
        def rootDir = System.getProperty('user.dir')

    }

    /**
     * Represents an exception thrown by this class.
     */
    static class RailwayException extends Exception {

        /**
         * Instantiates a new RailwayException
         *
         * @param message the message
         */
        RailwayException(@NotNull String message) {
            super(Objects.requireNonNull(message, 'Expected message to be not null'))
        }

    }

}
