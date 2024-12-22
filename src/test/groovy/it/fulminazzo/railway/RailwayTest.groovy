package it.fulminazzo.railway

import spock.lang.Specification

class RailwayTest extends Specification {
    static final PORT = 8123
    static final ROOT_DIR = 'build/resources/test'

    Railway railway

    def setup() {
        this.railway = new Railway(PORT, ROOT_DIR)
    }

    def tearDown() {
        this.railway.stop()
    }

    def 'test server'() {
        given:
        def path = 'content_handler'
        def url = new URL("http://localhost:${PORT}/${path}")
        HttpURLConnection connection = url.openConnection()

        when:
        this.railway.start()
        connection.setRequestMethod('GET')
        connection.connect()

        then:
        connection.getResponseCode() == 200
        connection.inputStream.bytes == new File("${ROOT_DIR}/${path}", 'index.html').bytes
    }

}
