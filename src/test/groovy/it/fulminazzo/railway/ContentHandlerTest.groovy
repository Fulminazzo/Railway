package it.fulminazzo.railway

import spock.lang.Specification

class ContentHandlerTest extends Specification {
    def contentHandler

    void setup() {
        this.contentHandler = new ContentHandler('build/resources/test')
    }

}
