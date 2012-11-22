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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParameterizedSocket {

    public static final String START_TOKEN      = "\u0001";
    public static final String END_TOKEN        = "\u0004";
    public static final String RECORD_SEPARATOR = "\u001E";
    public static final String FIELD_SEPARATOR  = "\u001F";

    public static final byte START_TOKEN_BYTE       = 0x01;
    public static final byte END_TOKEN_BYTE         = 0x04;
    public static final byte RECORD_SEPARATOR_BYTE  = 0x1E;
    public static final byte FIELD_SEPARATOR_BYTE   = 0x1F;
        
    public static class Message {
        List<String> parts;
        Map<String, String> namedParameters;

        public Message(List<String> parts, Map<String, String> namedParameters) {
            this.parts = parts;
            this.namedParameters = namedParameters; }
            
        public Message(String... parts) {
            this.parts = new ArrayList<String>();
            this.namedParameters = new HashMap<String, String>();
            
            for (String part : parts) { this.parts.add(part); }}

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (String part: parts) { sb.append(part + "/"); }
            for (Map.Entry param : namedParameters.entrySet()) {
                sb.append(param.getKey() + "=" + param.getValue()); }
            return sb.toString(); }
    }

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

    public void writeMessage(Message message) throws IOException {
        if (message == null || message.parts.size() == 0) return;

        byte[] messageBytes = formatMessage(message);
        socket.getOutputStream().write(messageBytes); }

    public Message readMessage() throws IOException {
        List<String> messageList = new ArrayList<String>();
        Map<String, String> namedParameters = new HashMap<String, String>();

        if (socket.getInputStream().read() != START_TOKEN_BYTE) {
            byte[] errMsg = formatMessage("ERROR", "Invalid command (expected START_TOKEN).");
            socket.getOutputStream().write(errMsg); }

        int bufIdx= 0;
        int nextByte = socket.getInputStream().read();
        String paramName = null;

        while (nextByte != END_TOKEN_BYTE) {

            // If we have reached end-of-stream, this is an error.
            if (nextByte == -1) {
                byte[] errMsg = formatMessage("ERROR", "Invalid command: stream ended before END_TOKEN was read.");
                socket.getOutputStream().write(errMsg);
                return null; }
            
            // Field separator means we have read a parameter name into the
            // buffer.
            else if (nextByte == FIELD_SEPARATOR_BYTE) {
                paramName = new String(buffer, 0, bufIdx, charset);
                bufIdx = 0; }

            // Record separator means we have finished reading the current
            // message part (named or unnamed parameter).
            else if (nextByte == RECORD_SEPARATOR_BYTE) {

                // No param name, must be an unnamed parameters.
                if (paramName == null) {
                    messageList.add(new String(buffer, 0, bufIdx, charset));
                    bufIdx = 0; }

                // If we read a param name we know this is a named parameters.
                else {
                    namedParameters.put(paramName,
                        new String(buffer, 0, bufIdx, charset));
                    bufIdx = 0;
                    paramName = null; } }

            else { buffer[bufIdx++] = (byte) nextByte; }

            nextByte = socket.getInputStream().read();
        }

        // If we have data in the buffer then there is a remaining parameter
        // (named or unnamed) to be read.
        if (bufIdx > 0) {
            if (paramName == null)
                messageList.add(new String(buffer, 0, bufIdx, charset));
            // If we read a param name we know this is a named parameters.
            else namedParameters.put(paramName,
                new String(buffer, 0, bufIdx, charset)); }

        return new Message(messageList, namedParameters); }

    protected byte[] formatMessage(String... parts) {
        return formatMessage(new Message(parts)); }

    protected byte[] formatMessage(Message message) {
        String fullMessage = START_TOKEN + message.parts.get(0);

        // Add all unnamed parameters.
        for (int i = 1; i < message.parts.size(); i++) {
            fullMessage += RECORD_SEPARATOR + message.parts.get(i); }

        // Add all named parameters.
        for (Map.Entry entry : message.namedParameters.entrySet()) {
            fullMessage += RECORD_SEPARATOR + entry.getKey().toString() +
                FIELD_SEPARATOR + entry.getValue().toString(); }

        fullMessage += END_TOKEN;
        return fullMessage.getBytes(charset); }
}
