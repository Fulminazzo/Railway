package it.fulminazzo.railway

import spock.lang.Ignore
import spock.lang.Specification

class RailwayTest extends Specification {
    static final PORT = 8123
    static final ROOT_DIR = 'build/resources/test'
    static final THREADS = 10

    static Railway railway

    def setupSpec() {
        railway = new Railway(PORT, THREADS, ROOT_DIR)
    }

    def cleanupSpec() {
        if (railway.isStarted()) railway.stop()
    }

    @Ignore
    def 'test server manually'() {
        when:
        railway.start()

        then:
        while (railway.isStarted());

    }

    def 'test get'() {
        given:
        def path = 'content_handler'
        def url = new URL("http://localhost:${PORT}/${path}")
        HttpURLConnection connection = url.openConnection() as HttpURLConnection

        when:
        railway.start()
        connection.setRequestMethod('GET')
        connection.connect()

        then:
        connection.getResponseCode() == 200
        connection.inputStream.bytes == new File("${ROOT_DIR}/${path}", 'index.html').bytes
    }

    def 'test invalid method'() {
        given:
        def path = 'content_handler'
        def url = new URL("http://localhost:${PORT}/${path}")
        HttpURLConnection connection = url.openConnection() as HttpURLConnection

        when:
        railway.start()
        connection.setRequestMethod(method)
        connection.connect()

        then:
        connection.getResponseCode() == 501

        where:
        method << ['POST', 'PUT', 'DELETE', 'OPTIONS']
    }

    def 'test getPort valid bounds'() {
        when:
        Railway.getPort(string)

        then:
        notThrown(RailwayException)

        where:
        string << ['0', '1', '2', '3', '65535']
    }

    def 'test getPort invalid bounds'() {
        when:
        Railway.getPort(string)

        then:
        thrown(RailwayException)

        where:
        string << [
                '-1', '65536',
                "${Integer.MIN_VALUE}".toString(),
                "${Integer.MAX_VALUE}".toString(),
        ]
    }

    def 'test getNatural valid bounds'() {
        when:
        Railway.getNatural(string)

        then:
        notThrown(RailwayException)

        where:
        string << ['0', '1', '2', '3']
    }

    def 'test getNatural invalid bounds'() {
        when:
        Railway.getNatural(string)

        then:
        thrown(RailwayException)

        where:
        string << ['-1', "${Integer.MIN_VALUE}".toString()]
    }

}
