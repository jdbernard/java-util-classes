/** # HTTP Context
  * @author Jonathan Bernard (jdbernard@gmail.com)
  * @copyright 2013 Jonathan Bernard. */
package com.jdbernard.net

import java.net.Socket
import java.net.URLEncoder
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.xml.bind.DatatypeConverter
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/** Helper class to perform basic HTTP communication. */
public class HttpContext {

    /** The host to which this object will connect. This also affects the
      * `Host` header in requests. */
    public String host = 'vbs-test'

    /** The port number.*/
    public int port = 80

    /** A cookie value to send in the request. This value will be automatically
      * set from any `Set-Cookie` header in the request response. */
    public String cookie

    /** HTTP Basic Authentication information. If this is a string, it will be
      * Base64 encoded as-is. Otherwise it may be an object with "username" and
      * "password" properties. The username and password will be concatenated
      * with a colon in between. The result will be Base64 encoded. */
    public def basicAuth

    /** Set this to `true` to use HTTPS. Otherwise, HTTP will be used. */
    public boolean secure = false

    /** The default Content-Type that we will send with our requests. This can
      * be overridden using the different method overloads. */
    public String defaultContentType = "application/json"

    private SSLSocketFactory sslSocketFactory = null;

    /** #### makeMessage
      * Make and return an HTTP request for the given HTTP method and URL. If
      * `request` is not null, it is expected to be an object which will be
      * converted to JSON and included as the request body. The `Host`,
      * `Cookie`, and `Content-Length` headers will be added based on the
      * `host` and `cookie` fields and the `request` object. */
    public String makeMessage(String method, String url, String contentType, def request) {
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

        if (basicAuth) {
            message.append("Authorization: Basic ")

            if (basicAuth instanceof String) message.append(
                DatatypeConverter.printBase64Binary(
                    (basicAuth as String).bytes))

            else message.append(
                DatatypeConverter.printBase64Binary(
                    "${basicAuth.username ?: ''}:${basicAuth.password ?: ''}".bytes)) 
            
            message.append("\r\n") }

        if (!contentType) contentType = "application.json"
        message.append("Content-Type: ")
        message.append(contentType)
        message.append("\r\n")

        if (request) {
            String requestBody
            if (contentType.startsWith("application/json") &&
                request instanceof Map) {
                def jsonRequestBuilder = new JsonBuilder(request)
                requestBody = jsonRequestBuilder.toString() }

            else if (contentType.startsWith("application/x-www-form-urlencoded") &&
                request instanceof Map)
                requestBody = urlEncode(request)

            else requestBody = request.toString()

            message.append("Content-Length: ")
            message.append(requestBody.length())
            message.append("\r\n\r\n")
            message.append(requestBody)

            message.append("\r\n") }
        else message.append("\r\n")

        return message.toString()
    }

   /** #### send
      * A wrapper around `makeMessage` and `send(String message)` to create a
      * request, send it, and return the response. This version allows you to
      * specify the request's Content-Type.*/
    public def send(String method, String url, String contentType, def request) {
        return send(makeMessage(method, url, contentType, request)) }

   /** #### send
      * A wrapper around `makeMessage` and `send(String message)` to create a
      * request, send it, and return the response. */
    public def send(String method, String url, def request) {
        return send(makeMessage(method, url, defaultContentType, request)) }

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

        Socket sock
        
        if (secure) {
            if (!sslSocketFactory)
                sslSocketFactory = SSLSocketFactory.getDefault()

            sock = sslSocketFactory.createSocket(host, port)
        }
        
        else sock = new Socket(host, port)

        def startTime

        sock.withStreams { strin, out ->
            def writer = new BufferedWriter(new OutputStreamWriter(out))
            def reader = new BufferedReader(new InputStreamReader(strin))
            int bytesExpected
            writer.write(message)
            startTime = System.currentTimeMillis()
            writer.flush()

            def line = reader.readLine().trim()
            result.status = line.split(/\s/)[1] as int
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

    /** #### post
      * A wrapper to perform a `POST` request. This calls
      * `send(String method, String url, def request)` with `method =
      * "POST"`. This version also allows you to set the request's
      * Content-Type. */
    public def post(String url, String contentType, def body) {
        return send('POST', url, contentType, body) }

    private String urlEncode(Map m) {
        List<String> parts = m.collect { k, v ->
            "$k=${URLEncoder.encode(v.toString(), 'UTF-8')}" }
        return parts.join("&") }

}
