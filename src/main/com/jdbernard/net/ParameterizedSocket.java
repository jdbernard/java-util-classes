/**
 * ParameterizedSocket
 * @author Jonathan Bernard (jdbernard@gmail.com)
 */
package com.jdbernard.net;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ParameterizedSocket {

    public static final String START_TOKEN = "\u0001";
    public static final String END_TOKEN = "\u0004";
    public static final String RECORD_SEPARATOR = "\u001E";

    public static final byte START_TOKEN_BYTE = 0x01;
    public static final byte END_TOKEN_BYTE = 0x04;
    public static final byte RECORD_SEPARATOR_BYTE = 0x1E;
        
    private Socket socket;
    private byte[] buffer;
    private Charset charset;

    public ParameterizedSocket(Socket socket, int bufferSize) {
        this.socket = socket;
        this.buffer = new byte[bufferSize];
        this.charset = Charset.forName("US-ASCII"); }

    public ParameterizedSocket(String ipAddress, int port, int bufferSize)
    throws UnknownHostException, IOException {
        this(new Socket(ipAddress, port), bufferSize); }

    public ParameterizedSocket(Socket socket)
    throws UnknownHostException, IOException { 
        this(socket, 2048); }

    public ParameterizedSocket(String ipAddress, int port)
    throws UnknownHostException, IOException {
        this(ipAddress, port, 2048); }

    public boolean getClosed() { return socket.isClosed(); }
    public boolean getConnected() { return socket.isConnected(); }

    public void close() throws IOException { socket.close(); }

    public void writeMessage(String... message) throws IOException {
        if (message == null || message.length == 0) return;

        byte[] messageBytes = formatMessage(message);
        socket.getOutputStream().write(messageBytes); }

    public String[] readMessage() throws IOException {
        List<String> messageList = new ArrayList<String>();

        if (socket.getInputStream().read() != START_TOKEN_BYTE) {
            byte[] errMsg = formatMessage("ERROR", "Invalid command (expected START_TOKEN).");
            socket.getOutputStream().write(errMsg); }

        int bufIdx= 0;
        int nextByte = socket.getInputStream().read();

        while (nextByte != END_TOKEN_BYTE) {
            if (nextByte == -1) {
                byte[] errMsg = formatMessage("ERROR", "Invalid command: stream ended before END_TOKEN was read.");
                socket.getOutputStream().write(errMsg);
                return null; }
            
            else if (nextByte == RECORD_SEPARATOR_BYTE) {
                messageList.add(new String(buffer, 0, bufIdx, charset));
                bufIdx = 0; }

            else { buffer[bufIdx++] = (byte) nextByte; }
        }

        if (bufIdx > 0) messageList.add(new String(buffer, 0, bufIdx, charset));

        return (String[]) messageList.toArray(); }

    protected byte[] formatMessage(String... message) {
        String fullMessage = START_TOKEN + message[0];
        for (int i = 1; i < message.length; i++) {
            fullMessage += RECORD_SEPARATOR + message[i]; }
        fullMessage += END_TOKEN;
        return fullMessage.getBytes(charset); }
}
