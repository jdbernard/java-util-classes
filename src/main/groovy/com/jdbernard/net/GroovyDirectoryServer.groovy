package com.jdbernard.net

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.*
import groovy.servlet.*
 
 public class GroovyDirectoryServer {

	public static void runJetty(int port) {
		def server = new Server(port)
	 
		def handler = new ServletContextHandler(ServletContextHandler.SESSIONS)
		handler.contextPath = '/'
		handler.resourceBase = '.'

		// Groovy Scripts
		handler.addServlet(GroovyServlet, '*.groovy')

		// Files
		def filesHolder = handler.addServlet(DefaultServlet, '/')
		filesHolder.setInitParameter('resourceBase', '.')
	 
		server.handler = handler
		server.start()
	}

	public static void main(String[] args) {
		def port = 9002
		 
		if (args.length < 1) { 
			port = 9002
			println "Defaulting to port 9002" }

		else try { port = args[0] as int }
		catch(Exception e) {
			println "Usage: GroovyDirectoryServer.grooy <port>"
			System.exit(1) }

		println "Starting Jetty on port $port, press Ctrl-C to stop."
		runJetty(port)
	}
}
