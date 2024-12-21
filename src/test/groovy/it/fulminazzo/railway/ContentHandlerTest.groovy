package it.fulminazzo.railway

import spock.lang.Specification

class ContentHandlerTest extends Specification {
    def contentHandler

    void setup() {
        this.contentHandler = new ContentHandler('build/resources/test')
    }

    def "test simple index.html"() {
        given:
        def path = "test_simple/index.html"

        when:
        def stream = this.contentHandler.parsePath(path)
        def read = new String(stream.bytes)

        then:
        read == "<!--It works!-->"
    }

    def "test no extension index.html"() {
        given:
        def path = "test_simple/index"

        when:
        def stream = this.contentHandler.parsePath(path)
        def read = new String(stream.bytes)

        then:
        read == "<!--It works!-->"
    }

}
