/*
 * Copyright 2013 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
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

package @MACRO.PACKAGE@;


import org.gsac.gsl.*;

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
import java.util.Properties;

import javax.servlet.*;
import javax.servlet.http.*;




/**
 *
 *
 */
public class @MACRO.PREFIX@Server extends GsacServer {

    /**
     * _more_
     *
     * @param args _more_
     * @throws Throwable _more_
     */
    public @MACRO.PREFIX@Server(String[] args) throws Throwable {
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
    public GsacServlet doMakeServlet(int port, Properties properties) throws Exception {
        return new GsacServlet(new @MACRO.PREFIX@Repository(), port, properties);
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
            @MACRO.PREFIX@Server jettyServer = new @MACRO.PREFIX@Server(args);
        } catch (Exception exc) {
            System.err.println("Error:" + exc);
            exc.printStackTrace();
            System.exit(1);
        }
    }



}
