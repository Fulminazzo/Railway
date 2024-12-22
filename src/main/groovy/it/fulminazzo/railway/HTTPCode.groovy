package it.fulminazzo.railway

import org.jetbrains.annotations.NotNull

/**
 * Represents all the possible codes returned by the server.
 */
enum HTTPCode {
    // 200
    OK(200),
    // 400
    NOT_FOUND(404),
    // 500
    NOT_IMPLEMENTED(501)
    ;

    int code

    /**
     * Instantiates a new HTTP code.
     *
     * @param code the code
     */
    HTTPCode(int code) {
        this.code = code
    }

    /**
     * Returns the message associated with the current code.
     *
     * @return the message
     */
    @NotNull String getMessage() {
        //TODO:
    }

}