/*
 * Copyright 2015 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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

/* CHANGE: make sure that the next 2 lines show your GSAC package name, replacing prototype */
package org.prototype.gsac;
import  org.prototype.gsac.database.*;

import org.gsac.gsl.*;
import java.io.*;
import java.util.Locale;
import java.util.Hashtable;
import java.util.Properties;

/*
 * CHANGE: 29 Jan 2015: merged GSAC java file version for either Java 1.6 or Java 1.7 GSAC build.
 * Look for " Java " here, in the next 40 lines, and un-comment the lines for the Java version you use.
*/ 

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* Jetty is used as the GSAC web application container in the case of no Tomcat. */

/*
 * Java 1.7 version:
 * Uncomment these lines to build a Prototype GSAC with Java 1.7
*/
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.handler.AbstractHandler;

/* 
 * Java 1.6 
 * Uncomment these lines to build a Prototype GSAC with Java 1.6
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
*/


/** 
 *
 *
 */
public class PrototypeServer extends GsacServer {

    /**
     * _more_
     *
     * @param args _more_
     * @throws Throwable _more_
     */
    public PrototypeServer(String[] args) throws Throwable {
        super(args);
    }


    /**
     * _more_
     *
     * @param port _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public GsacServlet doMakeServlet(int port, Properties properties)
            throws Exception {
        return new GsacServlet(port, properties);
    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Throwable _more_
     */
    public static void main(String[] args) throws Throwable {
        try {
            PrototypeServer jettyServer = new PrototypeServer(args);
        } catch (Exception exc) {
            System.err.println(" prototype jetty server main()  Error:" + exc);
            exc.printStackTrace();
            System.exit(1);
        }
    }

}
