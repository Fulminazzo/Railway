package it.fulminazzo.railway

import com.sun.net.httpserver.HttpServer
import lombok.Getter
import org.jetbrains.annotations.NotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * The main root class.
 */
class Railway {
    static final DEFAULT_PORT = 80
    static final DEFAULT_EXECUTOR_THREADS = 32

    final int port
    final int executorThreads
    final Logger logger
    final HttpServer server
    final ContentHandler contentHandler

    ExecutorService executor
    @Getter
    boolean started

    /**
     * Instantiates a new Railway server
     *
     * @param port the port
     * @param executorThreads the maximum amount of concurrent threads per request
     * @param rootDir the root directory
     */
    Railway(int port, int executorThreads, @NotNull String rootDir) {
        this.port = port
        this.executorThreads = executorThreads
        this.logger = LoggerFactory.getLogger(getClass().simpleName)
        this.server = HttpServer.create(new InetSocketAddress(port), 0)
        this.contentHandler = new ContentHandler(rootDir, this.logger)
    }

    /**
     * Starts the server.
     */
    void start() {
        if (isStarted()) throw new RailwayException('Server already started')

        this.started = true
        this.executor = Executors.newFixedThreadPool(this.executorThreads)

        this.server.createContext('/', this.contentHandler)
        this.server.setExecutor(this.executor)
        this.server.start()
    }

    /**
     * Stops the server.
     */
    void stop() {
        if (!isStarted()) throw new RailwayException('Server not started yet')

        this.server.stop(0)
        this.executor.shutdownNow()
    }

    /**
     * Main access point of the application.
     *
     * @param args the arguments passed
     */
    static void main(String[] args) {
        Railway server
        try {
            def port = DEFAULT_PORT
            def rootDir = System.getProperty('user.dir')
            def threads = DEFAULT_EXECUTOR_THREADS

            if (args.length > 0) {
                if (args[0] == '--help' || args[0] == "-h") {
                    println 'Usage: java -jar railway.jar <rootDir> <port> <threads>'
                    println 'The port and rootDir parameters are optional.'
                    return
                }
                rootDir = args[0]
            }
            if (args.length > 1) port = getPort(args[1])
            if (args.length > 2) threads = getNatural(args[2])

            server = new Railway(port, threads, rootDir)
            server.start()
        } catch (RailwayException | ContentHandlerException e) {
            System.err.println(e.message)
        } finally {
            if (server != null) server.stop()
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
        try {
            def tmp = getNatural(string)
            if (tmp >= 65536) throw new RailwayException('')
            else return tmp
        } catch (RailwayException ignored) {
            throw new RailwayException("Invalid port \"${string}\". A number between 0 and 65535 is required.")
        }
    }

    /**
     * Tries to get a natural number (higher than 0) from the given string.
     * A {@link RailwayException} is thrown in case of failure.
     *
     * @param string the string
     * @return the port
     */
    static int getNatural(@NotNull String string) throws RailwayException {
        if (string.isNumber()) {
            def tmp = string as Integer
            if (tmp >= 0) return tmp
        }
        throw new RailwayException("Invalid port \"${string}\". A number higher than 0 is required.")
    }

}
