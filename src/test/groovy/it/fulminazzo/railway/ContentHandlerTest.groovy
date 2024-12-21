package it.fulminazzo.railway

import spock.lang.Specification

class ContentHandlerTest extends Specification {
    def contentHandler

    void setup() {
        this.contentHandler = new ContentHandler('build/resources/test')
    }

    def "test path: #path"() {
        when:
        def stream = this.contentHandler.parsePath(path)
        def read = new String(stream.bytes)

        then:
        read == "<!--It works!-->"

        where:
        path << ["test_simple/index.html", "test_simple/index", "test_simple/", "test_simple"]
    }

    def "test style.css"() {
        given:
        def path = "test_simple/style.css"

        when:
        def stream = this.contentHandler.parsePath(path)
        def read = new String(stream.bytes)

        then:
        read == "/*It works!*/"
    }

}
