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
    static final CODES_MAP = [
            501 : "Not implemented"
    ]

    final @NotNull File root
    final @NotNull Logger logger

    /**
     * Instantiates a new ContextHandler
     *
     * @param rootDirPath the root directory path
     * @param logger      the logger
     */
    ContentHandler(@NotNull String rootDirPath, @NotNull Logger logger) {
        this.root = new File(rootDirPath)
        this.logger = logger
        if (!this.root.isDirectory())
            throw new ContentHandlerException('Could not find directory at path: ' + rootDirPath)
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
        def requesterIp = httpExchange.getRemoteAddress().getHostName()
        def method = httpExchange.requestMethod
        def path = httpExchange.requestURI.path
        def output = httpExchange.getResponseBody()
        Tuple response

        this.logger.info("${requesterIp} -> ${httpExchange.requestMethod} ${path}")

        switch (method) {
            case "GET" -> response = handleGET(httpExchange, path, output)
            default -> {
                response = getMessageFromCode(501)
                httpExchange.sendResponseHeaders(response[0], 0)
            }
        }

        this.logger.info("${requesterIp} <- ${response[0]} ${response[1]}")
        output.close()
    }

    /**
     * Handles a GET request.
     *
     * @param httpExchange the http exchange
     * @param path the path of the request
     * @param output the output where to write the body
     * @return a tuple containing the code and the returned path (for logging purposes)
     */
    Tuple handleGET(@NotNull HttpExchange httpExchange, @NotNull String path, @NotNull OutputStream output) {
        def response = 200
        File file
        try {
            file = resolvePath(path)
        } catch (ContentHandlerException e) {
            //TODO: 404 page
            throw new RuntimeException(e)
        }
        httpExchange.sendResponseHeaders(response, file.length())
        output << file.newInputStream()
        return new Tuple(response, file.getPath())
    }

    /**
     * Uses {@link #CODES_MAP} to get the respective message from the given code.
     *
     * @param code the code
     * @return the message from the code
     * @throws ContentHandlerException the exception thrown in case the message is not found
     */
    static Tuple getMessageFromCode(int code) throws ContentHandlerException {
        def message = CODES_MAP[code]
        if (message == null) throw new ContentHandlerException("Could not find error code ${code}")
        return new Tuple(code, message)
    }

}
