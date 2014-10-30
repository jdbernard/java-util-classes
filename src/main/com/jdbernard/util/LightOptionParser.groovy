/**
 * # Light Option Parser
 * @author Jonathan Bernard (jdbernard@gmail.com)
 *
 * LightOptionParser parses command-line style options.
 * TODO: complete docs.
 */
package com.jdbernard.util

public class LightOptionParser {

    public static def parseOptions(def optionDefinitions, String[] args) {
        return parseOptions(optionDefinitions, args as List<String>) }

    public static def parseOptions(def optionDefinitions, List<String> args) {

        def returnOpts = [args:[]]

        /// Look through each of the arguments to see if it is an option.
        /// Note that we are manually advancing the index in the loop.
        for (int i = 0; i < args.size();) {

            def retVal = false
            def optName = false

            if (args[i].startsWith('--')) optName = args[i].substring(2) 
            else if (args[i].startsWith('-')) optName = args[i].substring(1) 

            /// This was recognized as an option, try to find the definition
            /// and read in any arguments.
            if (optName) {

                /// Find the definition for this option.
                def optDef = optionDefinitions.find {
                    it.key == optName || it.value.longName == optName }

                if (!optDef) throw new IllegalArgumentException(
                    "Unrecognized option: '${args[i]}'.")

                optName = optDef.key
                optDef = optDef.value

                /// If there are no arguments, this is a flag. Set the value
                /// and advance the index.
                if ((optDef.arguments ?: 0) == 0) { retVal = true; i++ }

                /// If there are a pre-determined number of arguments, read them
                /// in.
                else if (optDef.arguments &&
                         optDef.arguments instanceof Number &&
                         optDef.arguments > 0) {

                    retVal = []

                    /// Not enough arguments left
                    if ((i + optDef.arguments) >= args.size()) {
                        throw new Exception("Option '${args[i]}' " +
                            "expects ${optDef.arguments} arguments.") }

                    /// Advance past the option onto the first argument.
                    i++

                    /// Copy the arguments
                    retVal += args[i..<(i + optDef.arguments)]

                    /// Advance the index past end of the arguements
                    i += optDef.arguments } 

                /// If there are a variable number of arguments, treat all
                /// arguments until the next argument or the end of options as
                /// arguments for this option
                else if (optDef.arguments == 'variable') {

                    retVal = []

                    /// Advance past the option to the first argument
                    i++

                    /// As long as we have not hit another option or the end of
                    /// arguments, keep adding arguments to the list for this
                    /// option.
                    for(;i < args.size() && !args[i].startsWith('-'); i++)
                        retVal << args[i] }

                else {
                    throw new Exception("Invalid number of arguments " +
                        "defined for option ${optName}. The number of " +
                        "arguments must be either an integer or the value " +
                        "'variable'") }

                /// Set the value on the option.
                if (retVal instanceof Boolean) {
                    returnOpts[optName] = retVal
                    if (optDef.longName) returnOpts[optDef.longName] = retVal }

                else {
                    if (!returnOpts.containsKey(optName))
                        returnOpts[optName] = []
                    returnOpts[optName] += retVal

                    if (optDef.longName) {
                        if (!returnOpts.containsKey(optDef.longName))
                            returnOpts[optDef.longName] = []
                        returnOpts[optDef.longName] += retVal } } }

            /// This was not as option, it is an unclaomed argument.
            else { returnOpts.args << args[i]; i++ } }

        /// Check that all required options have been found.
        optionDefinitions.each { optName, optDef ->
            /// If this is a required field
            if (optDef.required && 
               /// and it has not been found, by either it's short or long name.
               !(returnOpts[optName] ||
                (optDef.longName && returnOpts[optDef.longName]))) 
                
                throw new Exception("Missing required option: '-${optName}'.") }

        return returnOpts }
}
