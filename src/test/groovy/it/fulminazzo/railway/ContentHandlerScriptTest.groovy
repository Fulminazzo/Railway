package it.fulminazzo.railway

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import org.slf4j.LoggerFactory
import spock.lang.Specification

class ContentHandlerScriptTest extends Specification {
    final static String ROOT_DIR = 'build/resources/test'

    def 'test simple script'() {
        given:
        def file = new File(ROOT_DIR, 'scripts/index.groovy')
        def uri = new URI(path)
        def exchange = Mock(HttpExchange)
        exchange.requestURI >> uri

        when:
        def response = ContentHandler.runScript(file, exchange)
        def responseBody = new String(response.body.bytes)

        then:
        response.responseCode == code.code
        response.message == message
        responseBody == body

        where:
        path                            || code                 | message                  | body
        '/scripts/index.groovy/Michael' || HTTPCode.OK          | '/scripts/index.groovy'  | 'Hello, Michael!'
        '/scripts/index.groovy/'        || HTTPCode.BAD_REQUEST | '/scripts/index.groovy/' | 'No name provided!'
    }

    def 'test script does not override real method'() {
        given:
        def handler = new ContentHandler(ROOT_DIR, null, LoggerFactory.getLogger(getClass()))
        def file = new File(ROOT_DIR, 'scripts/index.groovy')
        def output = new ByteArrayOutputStream()
        def exchange = Mock(HttpExchange)
        exchange.requestURI >> new URI('scripts/')
        exchange.remoteAddress >> InetSocketAddress.createUnresolved('localhost', 1234)
        exchange.requestMethod >> 'GET'
        exchange.responseHeaders >> new Headers()
        exchange.responseBody >> output

        when:
        def response = handler.runScript(file, exchange)
        def responseBody = new String(response.body.bytes)
        handler.handle(exchange)

        then:
        responseBody == 'No name provided!'
        output.toString() == 'No name provided!'
    }

}