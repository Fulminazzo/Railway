package it.fulminazzo.railway

import com.sun.net.httpserver.HttpExchange
import org.jetbrains.annotations.NotNull
import org.slf4j.Logger

import java.util.function.Function

/**
 * Represents a general {@link Function} that executes the given {@link File} as a <b>Groovy script</b>.
 * It does so by first loading it to memory,
 * then for each call to the {@link ScriptCache#apply(HttpExchange)} function,
 * it checks whether the file was recently modified,
 * and updates the code by loading it again.
 */
class ScriptCache implements Function<HttpExchange, ContentHandler.HTTPResponse> {
    final File scriptFile
    final Logger logger
    Script script
    long lastModified

    /**
     * Instantiates a new Script cache.
     *
     * @param scriptFile the script file
     * @param logger     the logger
     */
    ScriptCache(@NotNull File scriptFile, @NotNull Logger logger) {
        this.scriptFile = Objects.requireNonNull(scriptFile, 'Expected script file to not be null')
        this.logger = Objects.requireNonNull(logger, 'Expected logger to not be null')
    }

    /**
     * Forcibly loads the script into memory.
     */
    void loadScript() {
        this.script = new GroovyShell().parse(this.scriptFile)
        this.lastModified = this.scriptFile.lastModified()
    }

    /**
     * Checks whether the file was modified,
     * if so it updates it using {@link #loadScript()}.
     */
    void checkUpdate() {
        if (this.scriptFile.lastModified() > this.lastModified) loadScript()
    }

    @Override
    ContentHandler.HTTPResponse apply(HttpExchange httpExchange) {
        if (httpExchange == null) throw new ContentHandlerException('Expected httpExchange to not be null')
        try {
            checkUpdate()
            return this.script.with { return handle(httpExchange) }
        } catch (Throwable e) {
            this.logger.error("Error was caught while executing script: ${this.scriptFile.path}", e)
            return new ContentHandler.HTTPResponse(HTTPCode.INTERNAL_SERVER_ERROR)
        }
    }

}
