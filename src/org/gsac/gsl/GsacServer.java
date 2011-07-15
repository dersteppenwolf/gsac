/*
 * Copyright 2010 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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


import org.mortbay.jetty.*;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.NCSARequestLog;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;


import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.*;
import org.mortbay.jetty.handler.AbstractHandler;

import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.io.*;



import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.*;
import javax.servlet.http.*;




/**
 * This implements a stand-alone gsac server. It uses the jetty servlet container.
 * Derived classes (e.g., org.unavco.projects.gsac.repository.UnavcoServer) can override
 * the doMakeServlet factory method to create a servlet with their own implementation of
 * the GsacRepository
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
        Properties properties = new Properties();
        int        port       = 8080;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-port")) {
                port = new Integer(args[i + 1]).intValue();
                i++;
            } else if (args[i].endsWith(".properties")) {
                properties.load(new FileInputStream(args[i]));
                //                System.err.println ("Loading properties:" + properties);
            } else if (args[i].startsWith("-D")) {
                //Look for -Dproperty=value arguments
                String[] toks = args[i].substring(2).split("=");
                if (toks.length != 2) {
                    throw new IllegalArgumentException("Bad argument:"
                            + args[i]);
                }
                properties.put(toks[0], toks[1]);
            }
        }
        GsacServlet              gsacServlet = doMakeServlet(port,
                                                             properties);

        Server                   server      = new Server(port);
        HandlerCollection        handlers    = new HandlerCollection();
        ContextHandlerCollection contexts    = new ContextHandlerCollection();
        Context context = new Context(contexts, "/", Context.SESSIONS);
        context.addServlet(new ServletHolder(gsacServlet),
                           gsacServlet.getRepository().getUrlBase() + "/*");
        handlers.setHandlers(new Handler[] { contexts,
                                             new DefaultHandler() });
        System.out.println(
            "Running stand-alone GSAC server at: http://localhost:" + port
            + GsacConstants.URL_BASE);
        server.setHandler(handlers);
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
        try {
            GsacServer jettyServer = new GsacServer(args);
        } catch (Exception exc) {
            System.err.println("Error:" + exc);
            exc.printStackTrace();
            System.exit(1);
        }
    }



}
