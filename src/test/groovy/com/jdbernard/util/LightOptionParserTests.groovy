package com.jdbernard.util

import groovy.util.GroovyTestCase

import org.junit.Test

import static com.jdbernard.util.LightOptionParser.parseOptions

public class LightOptionParserTests extends GroovyTestCase {

    def helpDef = ['h': [longName: 'help']]
    def confDef = ['c': [longName: 'config', required: true, arguments: 1]]
    def fullDef = [
        h: [longName: 'help'],
        c: [longName: 'config-file', required: true, arguments: 1],
        i: [longName: 'input-file', arguments: 'variable'],
        o: [longName: 'output-file2', arguments: 2]]

    void testShortFlagPresent1() { assert parseOptions(helpDef, ["-h"]).h }
    void testShortFlagPresent2() { assert parseOptions(helpDef, ["-h"]).help }
    void testLongFlagPresent() { assert parseOptions(helpDef, ["--help"]).h}
    void testShortFlagPresent() { assert parseOptions(helpDef, ["--help"]).help }

    void testFlagAbsent1() { assert !parseOptions(helpDef, ["arg"]).h }
    void testFlagAbsent2() { assert !parseOptions(helpDef, ["arg"]).help }

    void testRequiredOptionMissing() {
        try {
            parseOptions(confDef, ["arg"])
            assert false }
        catch (Exception e) {} }

    void testSingleArg1() {
        assert parseOptions(confDef, ["-c", "confFile"]).c == ["confFile"] }

    void testSingleArg2() {
        assert parseOptions(confDef, ["-c", "confFile"]).config == ["confFile"] }

    void testUnclaimedArgsAndFlag() {
        def opts = parseOptions(helpDef, ["arg1", "-h", "arg2"])
        assert opts.args == ["arg1", "arg2"] }

    void testUnclaimedAndClaimedArgs() {
        def opts = parseOptions(fullDef, ["-c", "confFile", "arg1"])
        assert opts.args == ["arg1"]
        assert opts.c == ["confFile"] }

    /*void testMultipleArgs1() {
        def opts = parseOptions(fullDef, ["-c", "confFile", ""])
        assert .conf == ["confFile"] }*/

    void testFull() {
        def opts = parseOptions(fullDef,
            ["-c", "cfgFile", "arg1", "-i", "in1", "in2", "in3",
             "-o", "out1", "out2", "arg2", "-h", "-i", "in4"])

        assert opts.h
        assert opts.c  == ["cfgFile"]
        assert opts['config-file'] == ["cfgFile"]
        assert opts.args == ["arg1", "arg2"]
        assert opts.i == [["in1", "in2", "in3"], ["in4"]]
        assert opts["input-file"] == [["in1", "in2", "in3"], ["in4"]]
        assert opts.o == [["out1", "out2"]]
        assert opts["output-file2"] == [["out1", "out2"]] }
}
