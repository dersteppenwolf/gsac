/*
 * Copyright 2014 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
 * http://www.unavco.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.gsac.gsl;

import java.io.*;
import java.util.Locale;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;



/**
 * 2010: This implements a stand-alone gsac server. It uses the jetty servlet container.
 * Derived classes (e.g., org.prototype.gsac.PrototypeServer) can override
 * the doMakeServlet factory method to create a servlet with their own implementation of
 * the GsacRepository.
 *
 * 23 Dec 2014: updated to javax servlet-api 3.0 and jetty 9.
 *
 */
public class GsacServer {

    /**
     * ctor
     *
     * @param args command line args
     * @throws Throwable On badness
     */
    public GsacServer(String[] args) throws Throwable {

        int port = 8080;

        Properties properties = new Properties();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-port")) {
                port = new Integer(args[i + 1]).intValue();
                i++;
            } else if (args[i].endsWith(".properties")) {
                if (new File(args[i]).exists()) {
                    properties.load(new FileInputStream(args[i]));
                } else {
                    //System.err.println("GSAC: property file not found: " + args[i]);
                }
            } else if (args[i].startsWith("-D")) {
                //Look for -Dproperty=value arguments
                String[] toks = args[i].substring(2).split("=");
                if (toks.length != 2) {
                    throw new IllegalArgumentException("Bad argument: " + args[i]);
                }
                properties.put(toks[0], toks[1]);
            }
        }

        GsacServlet              gsacServlet = doMakeServlet(port, properties);

        Server server = new Server(port);  

        ServletContextHandler contexthandler = new ServletContextHandler(ServletContextHandler.SESSIONS); 
        contexthandler.setContextPath("/"); // technically not required, as "/" is the default

        ServletHolder holderPwd = new ServletHolder("default", gsacServlet);

        contexthandler.addServlet(holderPwd, "/*");

        System.out.println( "Running stand-alone GSAC server at: http://localhost:" + port + gsacServlet.getRepository().getUrlBase());

        server.setHandler(contexthandler); 

        server.start(); 

        server.join(); 

    }
 

    /**
     * factory method to make the servlet
     *
     * @param port port
     * @param properties properties
     *
     * @return the servlet
     *
     * @throws Exception On badness
     */
    public GsacServlet doMakeServlet(int port, Properties properties)
            throws Exception {
        return new GsacServlet(port, properties);
    }

    /**
     * main
     *
     * @param args cmd line args
     *
     * @throws Throwable On badness
     */
    public static void main(String[] args) throws Throwable {

        // US numner formats: to force numerical output to use points (periods) before the fractional part of float numbers, 
        Locale.setDefault(new Locale("en", "US"));

        try {
            GsacServer jettyServer = new GsacServer(args);
        } catch (Exception exc) {
            System.err.println("Error:" + exc);
            exc.printStackTrace();
            System.exit(1);
        }
    }

}
