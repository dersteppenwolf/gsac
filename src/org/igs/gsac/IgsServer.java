/*
 *
 */

package org.igs.gsac;


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
public class IgsServer extends GsacServer {

    /**
     * _more_
     *
     * @param args _more_
     * @throws Throwable _more_
     */
    public IgsServer(String[] args) throws Throwable {
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
        return new GsacServlet(new IgsRepository(), port, properties);
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
            IgsServer jettyServer = new IgsServer(args);
        } catch (Exception exc) {
            System.err.println("Error:" + exc);
            exc.printStackTrace();
            System.exit(1);
        }
    }



}
