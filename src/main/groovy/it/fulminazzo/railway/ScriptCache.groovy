package it.fulminazzo.railway

import com.sun.net.httpserver.HttpExchange
import org.jetbrains.annotations.NotNull
import org.slf4j.Logger

import java.util.function.Function

class ScriptCache implements Function<HttpExchange, ContentHandler.HTTPResponse> {
    final File scriptFile
    final Logger logger
    Script script
    long lastModified

    ScriptCache(@NotNull File scriptFile, @NotNull Logger logger) {
        this.scriptFile = Objects.requireNonNull(scriptFile, 'Expected script file to not be null')
        this.logger = Objects.requireNonNull(logger, 'Expected logger to not be null')
    }

    def loadScript() {
        this.script = new GroovyShell().parse(this.scriptFile)
        this.lastModified = this.scriptFile.lastModified()
    }

    def checkUpdate() {
        if (this.scriptFile.lastModified() >= this.lastModified) loadScript()
    }

    @Override
    ContentHandler.HTTPResponse apply(HttpExchange httpExchange) {
        checkUpdate()
        return this.script.with { return handle(httpExchange) }
    }

}
