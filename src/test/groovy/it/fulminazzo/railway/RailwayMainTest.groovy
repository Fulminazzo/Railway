package it.fulminazzo.railway

import spock.lang.Specification

class RailwayMainTest extends Specification {
    ByteArrayOutputStream err
    PrintStream previousErr

    void setup() {
        this.previousErr = System.err
        this.err = new ByteArrayOutputStream()
        System.err = new PrintStream(this.err)
    }

    void cleanup() {
        System.err = this.previousErr
        System.err.println this.err.toString()
    }

}
