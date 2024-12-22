package it.fulminazzo.railway

import org.slf4j.LoggerFactory
import spock.lang.Specification

class ContentHandlerTest extends Specification {
    final static String ROOT_DIR = 'build/resources/test'

    def contentHandler

    void setup() {
        this.contentHandler = new ContentHandler(ROOT_DIR,
                null,
                LoggerFactory.getLogger(getClass()))
    }

    def 'test path: #path'() {
        when:
        def stream = this.contentHandler.resolvePath(path).newInputStream()
        def read = new String(stream.bytes)

        then:
        read == new String(new File(ROOT_DIR, 'content_handler/index.html').bytes)

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
        read == new String(new File(ROOT_DIR, path).bytes)
    }

}
