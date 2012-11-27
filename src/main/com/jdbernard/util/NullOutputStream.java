package com.jdbernard.util;

import java.io.OutputStream;

public class NullOutputStream extends OutputStream {

    public NullOutputStream() {}
    public void close() { }
    public void flush() { }
    public void write(int b) { }
    public void write(byte[] b) { }
    public void write(byte[] b, int offset, int length) { }
}
