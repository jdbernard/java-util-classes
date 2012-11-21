/**
 * LoggerOutputStream
 * @author Jonathan Bernard (jdbernard@gmail.com)
 */
package com.jdbernard.slf4j;

import java.io.IOException;
import java.io.OutputStream;
import org.slf4j.Logger;

public class LoggerOutputStream {

    public enum Level { TRACE, DEBUG, INFO, WARN, ERROR }

    private Logger logger;
    private Level level;
    private boolean closed = false;
    private byte[] buffer;
    private int count;

    private static final int DEFAULT_BUFFER_LENGTH = 2048;

    public LoggerOutputStream(Logger logger, Level level) {
        this.logger = logger;
        this.level = level; }

    /** {@inheritDoc} */
    public void write(final byte[] b, final int offset, final int length)
    throws IOException {
        if (closed)
            throw new IOException("The output stream has been closed.");

        // Grow the buffer if we will reach the limit.
        if (count + length >= buffer.length) {
            final int newLength =
                Math.max(buffer.length + DEFAULT_BUFFER_LENGTH, count + length);
            final byte[] newBuffer = new byte[newLength];

            System.arraycopy(buffer, 0, newBuffer, 0, count);
            buffer = newBuffer; }

        System.arraycopy(buffer, count, b, offset, length);
        count += length;
    }

    /** {@inheritDoc} */
    public void write(final int b) throws IOException {
        if (closed)
            throw new IOException("The output stream has been closed.");

        // Grow the buffer if we have reached the limit.
        if (count == buffer.length) {
            final int newLength = buffer.length + DEFAULT_BUFFER_LENGTH;
            final byte[] newBuffer = new byte[newLength];

            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer; }

        buffer[count++] = (byte) b;
    }

    /** {@inheritDoc} */
    public void flush() throws IOException {
        if (closed)
            throw new IOException("The output stream has been closed.");

        final byte[] messageBytes = new byte[count];
        System.arraycopy(buffer, 0, messageBytes, 0, count);
        final String message = new String(messageBytes);
        synchronized(logger) {
            switch (level) {
                default:
                case TRACE: logger.trace(message); break;
                case DEBUG: logger.debug(message); break;
                case INFO: logger.info(message); break;
                case WARN: logger.warn(message); break;
                case ERROR: logger.error(message); break;
            }
        }
        count = 0;
    }

    /** {@inheritDoc} */
    public void close() throws IOException {
        if (closed) return;
        flush();
        closed = true; }
}
