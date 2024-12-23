package it.fulminazzo.railway

import com.sun.net.httpserver.HttpExchange
import org.slf4j.LoggerFactory
import spock.lang.Specification

class ScriptCacheTest extends Specification {

    def 'test update system'() {
        given:
        def file = new File('build/resources/test/scripts/cache.groovy')
        if (file.isFile()) file.delete()
        def cache = new ScriptCache(file, LoggerFactory.getLogger(getClass()))
        def exchange = Mock(HttpExchange)

        when:
        file << generateFileContent(HTTPCode.OK)
        ContentHandler.HTTPResponse first = cache.apply(exchange)
        file.delete()
        file << generateFileContent(HTTPCode.NOT_FOUND)
        ContentHandler.HTTPResponse second = cache.apply(exchange)

        then:
        first.responseCode == HTTPCode.OK.code
        second.responseCode == HTTPCode.NOT_FOUND.code
    }

    String generateFileContent(HTTPCode code) {
        return 'static def handle(def httpExchange) { \n' +
                "    return new it.fulminazzo.railway.ContentHandler.HTTPResponse(it.fulminazzo.railway.HTTPCode.${code.name()})\n" +
                '}'
    }

}
