package it.fulminazzo.railway

import org.jetbrains.annotations.NotNull

/**
 * The main root class.
 */
class Railway {
    static final DEFAULT_PORT = 80

    // <port> <rootDir>
    static void main(String[] args) {
        try {
            def port = DEFAULT_PORT
            def rootDir = System.getProperty('user.dir')

            if (args.length > 0) port = getPort(args[0])
        } catch (RailwayException e) {
            System.err.println(e.message)
        }
    }

    /**
     * Tries to get a numeric port from the given string.
     * If the string is not a number between 0 and 65535,
     * a {@link RailwayException} is thrown.
     *
     * @param string the string
     * @return the port
     */
    static int getPort(@NotNull String string) throws RailwayException {
        if (string.isNumber()) {
            def tmp = string as Integer
            if (tmp >= 0 && tmp <= 65535)
                return tmp
        }
        throw new RailwayException("Invalid port \"${string}\". A number higher than 0 is required.")
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
