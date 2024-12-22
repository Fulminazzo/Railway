package it.fulminazzo.railway

import com.sun.net.httpserver.HttpServer
import lombok.Getter
import org.jetbrains.annotations.NotNull

/**
 * The main root class.
 */
class Railway {
    static final DEFAULT_PORT = 80

    final int port
    final HttpServer server
    final ContentHandler contentHandler
    @Getter
    boolean started

    /**
     * Instantiates a new Railway server
     *
     * @param port the port
     * @param rootDir the root directory
     */
    Railway(int port, @NotNull String rootDir) {
        this.port = port
        this.server = HttpServer.create(new InetSocketAddress(port), 0)
        this.contentHandler = new ContentHandler(rootDir)
    }

    /**
     * Starts the server.
     */
    void start() {
        if (isStarted()) throw new RailwayException('Server already started')
        this.started = true
        this.server.createContext('/', this.contentHandler)
        this.server.setExecutor(null)
        this.server.start()
    }

    /**
     * Stops the server.
     */
    void stop() {
        if (!isStarted()) throw new RailwayException('Server not started yet')
        this.server.stop(0)
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

            if (args.length > 0) {
                if (args[0] == '--help' || args[0] == "-h") {
                    println 'Usage: java -jar railway.jar <port> <rootDir>'
                    println 'The port and rootDir parameters are optional.'
                    return
                }
                port = getPort(args[0])
            }
            if (args.length > 1) rootDir = args[1]

            server = new Railway(port, rootDir)
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
        if (string.isNumber()) {
            def tmp = string as Integer
            if (tmp >= 0 && tmp <= 65535)
                return tmp
        }
        throw new RailwayException("Invalid port \"${string}\". A number higher than 0 is required.")
    }

}
