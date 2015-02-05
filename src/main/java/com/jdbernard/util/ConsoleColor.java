/**
* # ConsoleColor
* @author Jonathan Bernard (jdbernard@gmail.com)
* @org jdbernard.com/util/ConsoleColor
* @copyright 2010-2014 Jonathan Bernard
*/
package com.jdbernard.util;
/**
* The ConsoleColor class is a wrapper around [ANSI escape codes].
*
* [ANSI escape codes]: http://en.wikipedia.org/wiki/ANSI_escape_code
*/
public class ConsoleColor {

    // Storage for color information.
    public final Colors fg;
    public final Colors bg;
    public final boolean bright;

    public static enum Colors {
        BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE }

    public ConsoleColor(String propString) {
        String[] vals = propString.split("[,;: ]");

        fg = Colors.valueOf(vals[0]);

        if (vals.length == 2) {
            bg = null;
            bright = Boolean.parseBoolean(vals[1]); }

        else if (vals.length == 3) {
            bg = Colors.valueOf(vals[1]);
            bright = Boolean.parseBoolean(vals[2]); }
        
        else { bg = null; bright = false; } }

    public ConsoleColor(Colors fgColor) { this(fgColor, Colors.BLACK, false); }

    public ConsoleColor(Colors fgColor, boolean bright) {
        this(fgColor, Colors.BLACK, bright); }

    public ConsoleColor(Colors fgColor, Colors bgColor, boolean bright) {
        this.fg = fgColor; this.bg = bgColor; this.bright = bright; }

    public String toString() {
        String result = "\u001b[";

        if (bright) result += "1";
        else result += "0";

        if (fg != null) {
            result += ";";
            result += "3" + Integer.toString(fg.ordinal()); }

        if (bg != null) {
            result += ";";
            result += "4" + Integer.toString(bg.ordinal()); }

        return result + "m";
    }
}
