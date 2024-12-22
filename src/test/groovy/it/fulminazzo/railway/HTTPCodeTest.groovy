package it.fulminazzo.railway

import spock.lang.Specification

class HTTPCodeTest extends Specification {

    def "test getMessage method"() {
        expect:
        code.getMessage() == expected

        where:
        code                        || expected
        HTTPCode.OK                 || 'OK'
        HTTPCode.NOT_FOUND          || 'Not Found'
        HTTPCode.NOT_IMPLEMENTED    || 'Not Implemented'
    }

}
