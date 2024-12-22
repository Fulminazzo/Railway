package it.fulminazzo.railway

import spock.lang.Specification

class RailwayMainTest extends Specification {
    ByteArrayOutputStream out
    PrintStream previousOut
    ByteArrayOutputStream err
    PrintStream previousErr

    void setup() {
        this.previousOut = System.out
        this.out = new ByteArrayOutputStream()
        System.out = new PrintStream(this.out)
        this.previousErr = System.err
        this.err = new ByteArrayOutputStream()
        System.err = new PrintStream(this.err)
    }

    void cleanup() {
        System.out = this.previousOut
        System.out.println this.out.toString()
        System.err = this.previousErr
        System.err.println this.err.toString()
        Railway.railwayServer = null
    }

    def 'test normal functioning'() {
        given:
        Railway.main(new String[]{'build/resources/test', '8123', 'not_found.html', '2'})

        when:
        sleep 1000

        then:
        Railway.railwayServer.isStarted()
        Railway.railwayServer.stop()
    }

    def 'test normal functioning no arguments'() {
        expect:
        try {
            Railway.main()
            Railway.railwayServer.isStarted()
            Railway.railwayServer.stop()
        } catch (BindException e) {
            e.getMessage() == 'Permission denied'
        }
    }

    def 'test help'() {
        when:
        Railway.main(new String[]{argument})

        then:
        this.out.toString().contains('Usage')
        Railway.railwayServer == null

        where:
        argument << ['--help', '-h']
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
