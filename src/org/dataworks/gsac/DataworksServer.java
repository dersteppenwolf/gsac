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

package org.dataworks.gsac;


import org.gsac.gsl.*;


import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;

import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.io.*;
import java.util.Properties;



/**
 *
 *
 */
public class DataworksServer extends GsacServer {

    /**
     * _more_
     *
     * @param args _more_
     * @throws Throwable _more_
     */
    public DataworksServer(String[] args) throws Throwable {
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
        return new GsacServlet(new DataworksRepository(), port, properties);
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
            DataworksServer jettyServer = new DataworksServer(args);
        } catch (Exception exc) {
            System.err.println("Error:" + exc);
            exc.printStackTrace();
            System.exit(1);
        }
    }



}
