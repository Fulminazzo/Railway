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
        path << ['content_handler/index.html', 'content_handler/index',
                 'content_handler/', 'content_handler',
                 'content_handler/index/parameter?param=1']
    }

    def 'test case sensitive path'() {
        when:
        def path = 'BUILD/ReSoUrCeS/test/CONTENT_handler'
        def handler = new ContentHandler(System.getProperty('user.dir'), null, this.contentHandler.logger)
        def stream = handler.resolvePath(path).newInputStream()
        def read = new String(stream.bytes)

        then:
        read == new String(new File(ROOT_DIR, 'content_handler/index.html').bytes)
    }

    def 'test get file #path from root dir function'() {
        given:
        def handler = new ContentHandler(System.getProperty('user.dir'), null, this.contentHandler.logger)

        when:
        def file = handler.getFileFromRootDir(path)

        then:
        file == new File(expected).getAbsoluteFile()

        where:
        path                                             | expected
        'BUILD/ReSoUrCeS/test/CONTENT_handler'           | 'build/resources/test/content_handler'
        'build/resources/test/content_handler/STYLE.CSS' | 'build/resources/test/content_handler/style.css'
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

    def 'test invalid path'() {
        when:
        this.contentHandler.resolvePath(path)

        then:
        def e = thrown(ContentHandlerException)
        e.getMessage().contains(path)

        where:
        path << ['non_existing', 'not_found_dir', 'not_found_dir.txt', 'non_existing/path']
    }

    def 'test invalid root dir'() {
        when:
        new ContentHandler('not_existing', null, LoggerFactory.getLogger(getClass()))

        then:
        thrown(ContentHandlerException)
    }

    def 'test invalid not found file'() {
        when:
        new ContentHandler(ROOT_DIR, 'not_existing', LoggerFactory.getLogger(getClass()))

        then:
        thrown(ContentHandlerException)
    }

}
