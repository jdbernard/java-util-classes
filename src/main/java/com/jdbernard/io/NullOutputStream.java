/** # NullOutputStream
  * @author Jonathan Bernard (jdbernard@gmail.com)
  * @copyright 2011 Jonathan Bernard
  */
package com.jdbernard.util;

import java.io.OutputStream;

/** This implementation of OutputStream drops all data sent to it. */
public class NullOutputStream extends OutputStream {

    public NullOutputStream() {}
    public void close() { }
    public void flush() { }
    public void write(int b) { }
    public void write(byte[] b) { }
    public void write(byte[] b, int offset, int length) { }
}
