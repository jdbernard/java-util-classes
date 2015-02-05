package com.jdbernard.io;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.LinkedList;

public class NonBlockingInputStreamReader implements Runnable {

    private InputStream streamIn;
    private BufferedReader reader;
    private volatile boolean paused = false;
    private volatile boolean stopped = false;
    private LinkedList<String> buffer = new LinkedList<String>();

    public void run() {
        String line = null;
        while (!stopped && !Thread.currentThread().isInterrupted()) {
            try {
                if (paused) Thread.sleep(200);
                else {
                    line = reader.readLine();
                    if (line == null) stopped = true;
                    else storeLine(line); } }
            catch (IOException ioe) { stopped = true; }
            catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); } } }

    public synchronized String readLine() { return buffer.poll(); }
    private synchronized void storeLine(String line) { buffer.add(line); }

    public synchronized void pause() throws IOException { paused = true; }
    public synchronized void resume() throws IOException {
        reader.skip(streamIn.available());
        paused = false; }

    public NonBlockingInputStreamReader(InputStream streamIn) {
        this.streamIn = streamIn;
        this.reader = new BufferedReader(new InputStreamReader(streamIn)); }
}
