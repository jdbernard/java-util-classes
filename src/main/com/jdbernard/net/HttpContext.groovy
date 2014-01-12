/** # HTTP Context
  * @author Jonathan Bernard (jdbernard@gmail.com)
  * @copyright 2013 Jonathan Bernard. */
package com.jdbernard.net

import java.net.Socket
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/** Helper class to perform basic HTTP communication. */
public class HttpContext {

    /** The host to which this object will connect. This also affects the
      * `Host` header in requests. */
    public String host = 'vbs-test'

    /** The port number.*/
    public int port = 8000

    /** A cookie value to send in the request. This value will be automatically
      * set from any `Set-Cookie` header in the request response. */
    public String cookie

    /** #### makeMessage
      * Make and return an HTTP request for the given HTTP method and URL. If
      * `request` is not null, it is expected to be an object which will be
      * converted to JSON and included as the request body. The `Host`,
      * `Cookie`, and `Content-Length` headers will be added based on the
      * `host` and `cookie` fields and the `request` object. */
    public String makeMessage(String method, String url, def request) {
        StringBuilder message = new StringBuilder()
        message.append(method)
        message.append(" ")
        message.append(url)
        message.append(" HTTP/1.1\r\n")

        if (host && port) {
            message.append("Host: ")
            message.append(host)
            message.append(":")
            message.append(port)
            message.append("\r\n") }

        if (cookie) {
            message.append("Cookie: ")
            message.append(cookie)
            message.append("\r\n") }

        if (request) {
            String requestBody = new JsonBuilder(request).toString()

            message.append("Content-Type: application/json\r\n")
            message.append("Content-Length: ")
            message.append(requestBody.length())
            message.append("\r\n\r\n")
            message.append(requestBody) }

        message.append("\r\n")
        return message.toString()
    }

   /** #### send
      * A wrapper around `makeMessage` and `send(String message)` to create a
      * request, send it, and return the response. */
    public def send(String method, String url, def request) {
        return send(makeMessage(method, url, request)) }

    /** #### send
      * Send a message to the host specified by the object's `host` and `port`
      * fields and parses the response. Returns an object with the following
      * properties:
      *
      * - `status`: the HTTP response status (200, 404, etc.).
      * - `headers`: A list of header lines (one line per list entry).
      * - `content`: the response body (expected as JSON from the server) parsed
      *   into a Groovy object.
      * - `responseTime`: the time measured immediately after the client flushes
      *   its output buffer to the time it completely reads the server response.
      */
    public def send(String message) {
        Map result = [headers:[], content: null]
        Socket sock = new Socket(host, port)
        def startTime

        sock.withStreams { strin, out ->
            def writer = new BufferedWriter(new OutputStreamWriter(out))
            def reader = new BufferedReader(new InputStreamReader(strin))
            int bytesExpected
            writer.write(message)
            startTime = System.currentTimeMillis()
            writer.flush()

            def line = reader.readLine().trim()
            result.status = line.split(/\s/)[1]
            line = reader.readLine().trim()

            boolean isChunked = false

            while(line) {
                def m = (line =~ /Content-Length: (\d+)/)
                if (m) bytesExpected = m[0][1] as int

                m = (line =~ /Set-Cookie: ([^=]+=[^;]+);.+/)
                if (m) this.cookie = m[0][1]

                m = (line =~ /Transfer-Encoding: chunked/)
                if (m) isChunked = true

                result.headers << line
                line = reader.readLine().trim() }

            if (bytesExpected) {
                StringBuilder sb = new StringBuilder()
                for (int i = 0; i < bytesExpected; i++) {
                    sb.append(reader.read() as char) }

                result.responseTime = System.currentTimeMillis() - startTime
                
                try { result.content = new JsonSlurper().parseText(sb.toString()) }
                catch (Exception e) { result.content = sb.toString() } }
            else if (isChunked) {

                // Read chunks
                StringBuilder sb = new StringBuilder()
                while (true) {
                    line = reader.readLine().trim()

                    if (line == "0") break // end of chunks
                    // length of this chunk
                    else bytesExpected = Integer.parseInt(line.split(';')[0], 16)

                    for (int i = 0; i < bytesExpected; i++) {
                        sb.append(reader.read() as char) }

                    // Read CRLF
                    reader.readLine() }

                // Read any following headers.
                line = reader.readLine().trim()
                while (line) {
                    result.headers << line
                    line = reader.readLine().trim() }

                result.responseTime = System.currentTimeMillis() - startTime
                
                try { result.content = new JsonSlurper().parseText(sb.toString()) }
                catch (Exception e) { result.content = sb.toString() } }
            else 
                result.responseTime = System.currentTimeMillis() - startTime
        }

        sock.close()
        return result
    }

    /** #### get
      * A wrapper to perform a simple `GET` request. This calls
      * `send(String method, String url, def request)` with `method = "GET"`,
      * and `request = null`. */
    public def get(String url) { return send('GET', url, null) }

    /** #### post
      * A wrapper to perform a `POST` request. This calls
      * `send(String method, String url, def request)` with `method =
      * "POST"`. */
    public def post(String url, def body) { return send('POST', url, body) }
}
