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

    def 'test invalid arguments'() {
        when:
        Railway.main(args.toArray(new String[0]))

        then:
        this.err.toString() == "${expected}\n"

        where:
        expected || args
        'Invalid port \"a\". A number between 0 and 65535 is required.' || ['build', 'a']
        'Invalid port \"-1\". A number between 0 and 65535 is required.' || ['build', '-1']
        'Invalid port \"65536\". A number between 0 and 65535 is required.' || ['build', '65536']
        'Invalid natural \"a\". A number higher than 0 is required.' || ['build', '2', 'not_found.html', 'a']
        'Invalid natural \"-1\". A number higher than 0 is required.' || ['build', '2', 'not_found.html', '-1']
    }

}
