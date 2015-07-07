#!/usr/bin/env groovy

package com.jdbernard.net

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.*
import groovy.servlet.*
 

def startJetty(int port) {
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
 
 if (args.length < 1) {
 	println "Usage: webServer.groovy <port>"
	System.exit(1) }

println "Starting Jetty, press Ctrl-C to stop."
startJetty(Integer.parseInt(args[0]))
