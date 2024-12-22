package it.fulminazzo.railway

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import org.jetbrains.annotations.NotNull
import org.slf4j.Logger

/**
 * A class responsible for correctly handling paths and returning appropriate files.
 */
class ContentHandler implements HttpHandler {
    static final INDEX_NAME = 'index.html'

    final @NotNull File root
    final @NotNull File notFoundPage
    final @NotNull Logger logger

    /**
     * Instantiates a new ContextHandler
     *
     * @param rootDirPath  the root directory path
     * @param notFoundPage the page that represents the not found page
     * @param logger       the logger
     */
    ContentHandler(@NotNull String rootDirPath, @NotNull String notFoundPage, @NotNull Logger logger) {
        this.root = new File(rootDirPath)
        if (!this.root.isDirectory())
            throw new ContentHandlerException('Could not find directory at path: ' + rootDirPath)
        File notFound = new File(notFoundPage)
        if (!notFound.isFile()) notFound = new File(this.root, notFoundPage)
        if (!notFound.isFile())
            throw new ContentHandlerException('Could not find not found page at path: ' + notFoundPage)
        this.notFoundPage = notFound
        this.logger = logger
    }

    /**
     * Resolves the given path by:
     * <ul>
     *     <li>if it contains a file extension, it searches for the file in the file system;</li>
     *     <li>if it does not contain, it searches for the file <code>&lt;path&gt;.html</code> in the file system;</li>
     *     <li>if it is a directory, it searches for the file <code>&lt;path&gt;/index.html</code> in the file system.</li>
     * <ul>
     * In every other case, it throws an exception.
     *
     * @param path the path
     * @return the file
     * @throws ContentHandlerException the exception thrown in case of error
     */
    @NotNull File resolvePath(@NotNull String path) throws ContentHandlerException {
        def file = new File(this.root, path)
        if (file.isFile()) return file
        else if (file.isDirectory()) {
            file = new File(file, INDEX_NAME)
            if (file.isFile()) return file
            else throw new ContentHandlerException('Could not find path: ' + path)
        } else {
            def pattern = ~/(.*)\.([^.]+)/
            def matcher = path =~ pattern
            if (matcher.matches())
                throw new ContentHandlerException('Could not find path: ' + path)
            else {
                file = new File(this.root, path + '.html')
                if (file.isFile()) return file
                else throw new ContentHandlerException('Could not find path: ' + path)
            }
        }
    }

    @Override
    void handle(HttpExchange httpExchange) throws IOException {
        def requesterIp = httpExchange.remoteAddress.hostName
        def method = httpExchange.requestMethod
        def path = httpExchange.requestURI.path
        HTTPResponse response

        this.logger.info("${requesterIp} -> ${httpExchange.requestMethod} ${path}")

        switch (method) {
            case 'GET' -> response = handleGET(path)
            default -> response = new HTTPResponse(HTTPCode.NOT_IMPLEMENTED)
        }

        def responseCode = response.responseCode
        def responseBody = response.body
        def output = httpExchange.responseBody

        this.logger.info("${requesterIp} <- ${responseCode} ${response.message}")

        httpExchange.sendResponseHeaders(responseCode, responseBody.available())
        output << responseBody
        output.close()
    }

    /**
     * Handles a GET request.
     *
     * @param httpExchange the http exchange
     * @param path the path of the request
     * @param output the output where to write the body
     * @return the HTTP response
     */
    @NotNull HTTPResponse handleGET(@NotNull String path) {
        HTTPCode code
        File file
        try {
            code = HTTPCode.OK
            file = resolvePath(path)
        } catch (ContentHandlerException e) {
            //TODO: 404 page
            throw new RuntimeException(e)
        }
        return new HTTPResponse(code, file.getPath(), file.newInputStream())
    }

    /**
     * Represents a holder for contents of a HTTP response.
     */
    static class HTTPResponse {
        final int responseCode
        final String message
        final InputStream body

        /**
         * Instantiates a new HTTP response
         *
         * @param responseCode the code
         * @param message      the message displayed by the logger
         * @param body         the body itself
         */
        HTTPResponse(@NotNull HTTPCode responseCode, @NotNull String message, @NotNull InputStream body) {
            this.responseCode = responseCode.code
            this.message = Objects.requireNonNull(message, 'Expected message to not be null')
            this.body = Objects.requireNonNull(body, 'Expected body to not be null')
        }

        /**
         * Instantiates a new HTTP response
         *
         * @param responseCode the code
         * @param message      the message displayed by the logger
         * @param body         the body itself
         */
        HTTPResponse(@NotNull HTTPCode responseCode, @NotNull String message, @NotNull String body) {
            this(responseCode, message, new ByteArrayInputStream(body.bytes))
        }

        /**
         * Instantiates a new HTTP response
         *
         * @param responseCode the code
         */
        HTTPResponse(@NotNull HTTPCode responseCode) {
            this(responseCode, responseCode.getMessage(), '')
        }

    }

}
