/**
 * # Light Option Parser
 * @author Jonathan Bernard (jdbernard@gmail.com)
 * @org util.jdbernard.com/LightOptionParser
 *
 * LightOptionParser is a small utility class which parses command-line style
 * options.
 * 
 */
package com.jdbernard.util

/**
 * ## Option Definitions
 * 
 * LightOptionParser uses a option definition map to define its parsing
 * behavior. Each entry in the map represents an option that the parser should
 * recognize. The entry's key is expected to be the short version of the
 * option, which will be recognized when preceded by one hyphen. The value for
 * the entry is another map containing the detailed definition of that option.
 *
 * Valid values for an option definition map are:
 *
 * longName
 * :   The long version of the option name. This is recognized by the parser
 *     when preceded by two hyphens (`longName: 'help'` corresponds to `--help`
 *     for example). There is no default for this value. If it is not given
 *     then the parser will not recognize this option by any long name.
 *
 * required
 * :   A boolean value indicating whether the parser should require that this
 *     option be present when parsing a set of arguments. This value defaults
 *     to false.
 *
 * arguments
 * :   A value representing the number of arguments this option is expected to
 *     take. This can be an integer represening a fixed number of arguments or
 *     the string value `variable`. If `variable` is given the parser will
 *     associate every subsequent argument with this option until it encounters
 *     another option or runs out of arguments. If this value is given ant is
 *     not either an integer or the string `variable` then an Exception will be
 *     thrown. This value defaults to 0.
 *
 * Here is an example of an option definitions map:
 *
 *     def optDefs = [
 *         'h': [longName: 'help'],
 *         'v': [longName: 'version'],
 *         'd': [longName: 'working-dir', arguments: 1],
 *         'p': [longName: 'file-patterns', arguments: variable, required:
 *               true] 
 *     ]
 */
public class LightOptionParser {

    public static def parseOptions(def optionDefinitions, String[] args) {
        return parseOptions(optionDefinitions, args as List<String>) }

    /**
     * ### parseOptions
     * Parse a set of arguments according to a given a map of option
     * definitions. This will return a map of options found in the given
     * arguments. Each entry in this map represents an option that was found.
     * The key will be the option name. If the option takes arguments then the
     * value will be an array of the arguments found. For the sake of
     * consistency this will always be an array, even if the option takes only
     * one argument. If the option takes no arguments, the value will be
     * `true`.
     *
     * If a found option was defined with a long name then the returned map
     * will have entries for both the short and long names of the option. The
     * values of these entries will be identical.
     *
     * If an option is defined with a fixed number of *n* arguments then the
     * parser will treat the next *n* arguments following the option as
     * arguments to that option, regardless of their value. For example, if I
     * define an option `arg2` that takes 2 arguments and pass 
     * `--arg2 first -f` as arguments, the `-f` will be taken as the second
     * argument to the `arg2` option, not a separate option.
     *
     * This method throws an IllegalArgumentException when an option is found
     * that does not match any of the given option definitions.
     *
     * This method throws an Exception if an option requires more arguments
     * than are remaining or an option that is defined as required is not
     * present.
     *
     * @org util.jdbernard.com/LightOptionParser/parseOptions
     */
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
