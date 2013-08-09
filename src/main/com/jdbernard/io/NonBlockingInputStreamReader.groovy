package com.jdbernard.io;

public class NonBlockingInputStreamReader implements Runnable {

    private Reader rin
    private LinkedList buffer = []

    public void run() {
        String line = null
        try {
            while((line = rin.readLine()) != null &&
                !Thread.currentThread().isInterrupted())
                storeLine(line) }
        catch (InterruptedException ie) { Thread.currentThread().interrupt() } }

    public synchronized String readLine() { return buffer.poll() }
    private synchronized void storeLine(String line) { buffer << line }

    public NonBlockingInputStreamReader(def sin) {
        this.rin = new InputStreamReader(sin) }
}
