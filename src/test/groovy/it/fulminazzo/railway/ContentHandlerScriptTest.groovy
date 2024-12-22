package it.fulminazzo.railway

import com.sun.net.httpserver.HttpExchange
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

}