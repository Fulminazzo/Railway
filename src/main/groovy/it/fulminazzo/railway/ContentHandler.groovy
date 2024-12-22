package it.fulminazzo.railway

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.slf4j.Logger

/**
 * A class responsible for correctly handling paths and returning appropriate files.
 */
class ContentHandler implements HttpHandler {
    static final INDEX_NAME = 'index'
    static final SCRIPTS_EXTENSION = 'groovy'
    static final INDEXES_EXTENSIONS = ['html', SCRIPTS_EXTENSION]
    static final SERVER_NAME_VERSION = 'Railway/1.0'

    final @NotNull File root
    final @Nullable File notFoundPage
    final @NotNull Logger logger

    /**
     * Instantiates a new ContextHandler
     *
     * @param rootDirPath  the root directory path
     * @param notFoundPage the page that represents the not found page (null if none)
     * @param logger       the logger
     */
    ContentHandler(@NotNull String rootDirPath, @Nullable String notFoundPage, @NotNull Logger logger) {
        this.root = new File(rootDirPath)
        if (!this.root.isDirectory())
            throw new ContentHandlerException('Could not find directory at path: ' + rootDirPath)
        if (notFoundPage == null) this.notFoundPage = null
        else {
            File notFound = new File(notFoundPage)
            if (!notFound.isFile()) notFound = new File(this.root, notFoundPage)
            if (!notFound.isFile())
                throw new ContentHandlerException('Could not find not found page at path: ' + notFoundPage)
            this.notFoundPage = notFound
        }
        this.logger = logger
    }

    /**
     * Resolves the given path using {@link #resolvePathSingle(String)}.
     * If it could not be solved, it tries to cut the path using forward slashes
     * and tries the resolution once again.
     *
     * @param path the path
     * @return the file
     * @throws ContentHandlerException the exception thrown in case of error
     */
    @NotNull File resolvePath(@NotNull String path) throws ContentHandlerException {
        try {
            return resolvePathSingle(path)
        } catch (ContentHandlerException e) {
            def matcher = path =~ /([^?]+)\?.*/
            try {
                if (matcher.find()) return resolvePath(matcher.group(1))
            } catch (ContentHandlerException ignored) {}
            matcher = path =~ /([^\/]*)\/[^\/]+/
            try {
                if (matcher.find()) {
                    def group = matcher.group(1)
                    if (!group.trim().isEmpty()) return resolvePath(group)
                }
            } catch (ContentHandlerException ignored) {}
            throw e
        }
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
    @NotNull File resolvePathSingle(@NotNull String path) throws ContentHandlerException {
        def file = getFileFromRootDir(path)
        if (file.isFile()) return file
        else if (file.isDirectory()) {
            for (def ext : INDEXES_EXTENSIONS) {
                def f = getFileFromRootDir("${path}${File.separator}${INDEX_NAME}.${ext}")
                if (f.isFile()) return f
            }
            throw new ContentHandlerException('Could not find path: ' + path)
        } else {
            def pattern = ~/(.*)\.([^.]+)/
            def matcher = path =~ pattern
            if (matcher.matches())
                throw new ContentHandlerException('Could not find path: ' + path)
            else {
                for (def ext : INDEXES_EXTENSIONS) {
                    file = getFileFromRootDir("${path}.${ext}")
                    if (file.isFile()) return file
                }
                throw new ContentHandlerException('Could not find path: ' + path)
            }
        }
    }

    /**
     * Tries to get the given path from the {@link #root} directory.
     * This function is CaSe InSeNsItIvE.
     *
     * @param path the path
     * @return the file (if not found, will return a file with the remaining not found path)
     */
    @NotNull File getFileFromRootDir(@NotNull String path) {
        def tmp = path.split('/').findAll{ !it.isEmpty() }
        def file = this.root
        for (i in 0 .. tmp.size() - 1) {
            if (file.isDirectory()) {
                def files = file.listFiles()
                if (files != null) {
                    def curr = tmp[i]
                    def actual = files.collect {  it.name }.find { curr.equalsIgnoreCase(it) }
                    if (actual != null) {
                        file = new File(file, actual)
                        continue
                    }
                }
            }
            return new File(file, tmp[i .. -1].join('/'))
        }
        return file
    }

    @Override
    void handle(HttpExchange httpExchange) throws IOException {
        def requesterIp = httpExchange.remoteAddress.hostName
        def method = httpExchange.requestMethod
        def path = httpExchange.requestURI.path
        HTTPResponse response

        this.logger.info("${requesterIp} -> ${httpExchange.requestMethod} ${path}")

        switch (method) {
            case 'GET' -> response = handleGET(httpExchange, path)
            default -> response = new HTTPResponse(HTTPCode.NOT_IMPLEMENTED)
        }

        def responseCode = response.responseCode
        def responseBody = response.body
        def output = httpExchange.responseBody

        this.logger.info("${requesterIp} <- ${responseCode} ${response.message}")

        response.headers.forEach { k, v -> httpExchange.responseHeaders.put(k, v) }
        httpExchange.responseHeaders.put('Server', [SERVER_NAME_VERSION])
        httpExchange.sendResponseHeaders(responseCode, responseBody.available())
        output << responseBody
        output.close()
    }

    /**
     * Handles a GET request.
     *
     * @param httpExchange the http exchange
     * @param path the path of the request
     * @return the HTTP response
     */
    @NotNull HTTPResponse handleGET(@NotNull HttpExchange httpExchange, @NotNull String path) {
        HTTPCode code
        File file
        try {
            code = HTTPCode.OK
            file = resolvePath(path)
        } catch (ContentHandlerException ignored) {
            code = HTTPCode.NOT_FOUND
            file = this.notFoundPage
        }
        if (file == null) return new HTTPResponse(code)
        else if (file.getName().endsWith(".${SCRIPTS_EXTENSION}")) return runScript(file, httpExchange)
        else return new HTTPResponse(code, file.getPath(), file.newInputStream())
    }

    /**
     * Connects to the given link using the specified method and headers.
     * Then, returns the body received.
     *
     * @param link    the link
     * @param method  the HTTP method
     * @return the body
     * @throws ContentHandlerException thrown in case of any error
     */
    static @NotNull InputStream requestWebsite(@NotNull String link, @NotNull String method) throws ContentHandlerException {
        requestWebsite(link, method, null)
    }

    /**
     * Connects to the given link using the specified method and headers.
     * Then, returns the body received.
     *
     * @param link    the link
     * @param method  the HTTP method
     * @param headers the headers to use for the connection
     * @return the body
     * @throws ContentHandlerException thrown in case of any error
     */
    static @NotNull InputStream requestWebsite(@NotNull String link, @NotNull String method,
                                               @Nullable Map<String, String> headers) throws ContentHandlerException {
        try {
            def url = new URL(link)
            HttpURLConnection connection = url.openConnection() as HttpURLConnection
            connection.setRequestMethod(method)
            if (headers != null) headers.forEach { k, v -> connection.setRequestProperty(k, v) }
            connection.connect()

            return connection.inputStream
        } catch (IOException e) {
            throw new ContentHandlerException("Could not ${method} website: ${link}", e)
        }
    }

    /**
     * Runs the given script by invoking the method <code>handle(HttpExchange)</code>.
     * If that method is not present, an exception is thrown.
     *
     * @param scriptFile   the script file
     * @param httpExchange the http exchange
     * @return the HTTPResponse returned by the method
     */
    @NotNull HTTPResponse runScript(@NotNull File scriptFile, @NotNull HttpExchange httpExchange) {
        if (httpExchange == null) throw new ContentHandlerException('Expected httpExchange to not be null')
        return new GroovyShell().parse(scriptFile).with {
            return handle(httpExchange)
        }
    }

    /**
     * Represents a holder for contents of a HTTP response.
     */
    static class HTTPResponse {
        final int responseCode
        final Map<String, List<String>> headers
        final String message
        final InputStream body

        /**
         * Instantiates a new HTTP response
         *
         * @param responseCode the code
         * @param message      the message displayed by the logger
         * @param body         the body itself
         * @param headers      the headers for the response
         */
        HTTPResponse(@NotNull HTTPCode responseCode, @NotNull String message, @NotNull InputStream body,
                     @Nullable Map<String, List<String>> headers) {
            this.responseCode = responseCode.code
            this.message = Objects.requireNonNull(message, 'Expected message to not be null')
            this.body = Objects.requireNonNull(body, 'Expected body to not be null')
            this.headers = new LinkedHashMap<>()
            if (headers != null) this.headers.putAll(headers)
        }

        /**
         * Instantiates a new HTTP response
         *
         * @param responseCode the code
         * @param message      the message displayed by the logger
         * @param body         the body itself
         */
        HTTPResponse(@NotNull HTTPCode responseCode, @NotNull String message, @NotNull InputStream body) {
            this(responseCode, message, body, null)
        }

        /**
         * Instantiates a new HTTP response
         *
         * @param responseCode the code
         * @param message      the message displayed by the logger
         * @param body         the body itself
         * @param headers      the headers for the response
         */
        HTTPResponse(@NotNull HTTPCode responseCode, @NotNull String message, @NotNull String body,
                     @Nullable Map<String, List<String>> headers) {
            this(responseCode, message, new ByteArrayInputStream(body.bytes), headers)
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
