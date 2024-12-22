package it.fulminazzo.railway

import org.slf4j.LoggerFactory
import spock.lang.Specification

class ContentHandlerTest extends Specification {
    def contentHandler

    void setup() {
        this.contentHandler = new ContentHandler('build/resources/test',
                LoggerFactory.getLogger(getClass()))
    }

    def 'test path: #path'() {
        when:
        def stream = this.contentHandler.resolvePath(path).newInputStream()
        def read = new String(stream.bytes)

        then:
        read == '<!--It works!-->'

        where:
        path << ['content_handler/index.html', 'content_handler/index', 'content_handler/', 'content_handler']
    }

    def 'test style.css'() {
        given:
        def path = 'content_handler/style.css'

        when:
        def stream = this.contentHandler.resolvePath(path).newInputStream()
        def read = new String(stream.bytes)

        then:
        read == '/*It works!*/'
    }

}
