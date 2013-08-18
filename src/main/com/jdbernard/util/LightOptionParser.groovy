/**
 * # Light Option Parser
 * @author Jonathan Bernard (jdbernard@gmail.com)
 *
 * LightOptionParser parses command-line style options.
 * TODO: complete docs.
 */
package com.jdbernard.util

public class LightOptionParser {

    public static def parseOptions(def optionDefinitions, List args) {

        def returnOpts = [:]
        def foundOpts = [:]
        def optionArgIndices = []

        /// Find all the options.
        args.eachWithIndex { arg, idx ->
            if (arg.startsWith('--')) foundOpts[arg.substring(2)] = [idx: idx]
            else if (arg.startsWith('-')) foundOpts[arg.substring(1)] = [idx: idx] }

        /// Look for option arguments.
        foundOpts.each { foundName, optInfo ->

            def retVal

            /// Find the definition for this option.
            def optDef = optionDefinitions.find {
                it.key == foundName || it.value.longName == foundName }

            if (!optDef) throw new IllegalArgumentException(
                "Unrecognized option: '${args[optInfo.idx]}.")

            def optName = optDef.key
            optDef = optDef.value

            /// Remember the option index for later.
            optionArgIndices << optInfo.idx

            /// If there are no arguments, this is a flag.
            if ((optDef.arguments ?: 0) == 0) retVal = true

            /// Otherwise, read in the arguments
            if (optDef.arguments && optDef.arguments > 0) {

                /// Not enough arguments left
                if ((optInfo.idx + optDef.arguments) >= args.size()) {
                    throw new Exception("Option '${args[optInfo.idx]}' " +
                        "expects ${optDef.arguments} arguments.") }

                int firstArgIdx = optInfo.idx + 1

                /// Case of only one argument
                if (optDef.arguments == 1)
                    retVal = args[firstArgIdx]
                /// Case of multiple arguments
                else retVal = args[firstArgIdx..<(firstArgIdx + optDef.arguments)]

                /// Remember all the option argument indices for later.
                (firstArgIdx..<(firstArgIdx + optDef.arguments)).each {
                    optionArgIndices << it }}

            /// Store the value in the returnOpts map
            returnOpts[optName] = retVal
            if (optDef.longName) returnOpts[optDef.longName] = retVal }

        /// Check that all required options have been found.
        optionDefinitions.each { optName, optDef ->
            /// If this is a required field
            if (optDef.required && 
               /// and it has not been found, by either it's short or long name.
               !(returnOpts[optName] ||
                (optDef.longName && returnOpts[longName]))) 
                
                throw new Exception("Missing required option: '-${optName}'.") }

        /// Remove all the option arguments from the args list and return just
        /// the non-option arguments.
        optionArgIndices.sort().reverse().each { args.remove(it) }

        //optionArgIndices = optionArgIndices.collect { args[it] }
        //args.removeAll(optionArgIndices)
            
        returnOpts.args = args
        return returnOpts }
}
