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

package org.gsac.gsl.test;


import org.gsac.gsl.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.*;


/**
 * Tester
 */
public class GsacTest implements Runnable {

    /** _more_ */
    static String[] URLS = { "http://${server}/gsacws/gsacapi/site/view?site.id=18126_P100",
                             "http://${server}/gsacws/gsacapi/site/search/sites.kml?output=site.kml&limit=1000&site.code=p12*",
                             "http://${server}/gsacws/gsacapi/site/search/sites.csv?output=site.csv&limit=1000&site.code=p12*",
                             "http://${server}/gsacws/gsacapi/site/search?limit=1000&site.code=p*" };

    /** _more_ */
    String[] urls;

    /** _more_ */
    static int cnt = 0;

    /** _more_ */
    static int NUM_THREADS = 2;

    /** _more_ */
    private String server;

    /** _more_ */
    private static long startTime;

    /** _more_ */
    private long pause = 0;

    /**
     * _more_
     *
     * @param server _more_
     * @param urls _more_
     * @param pause _more_
     */
    public GsacTest(String server, String[] urls, long pause) {
        this.server = server;
        this.urls   = urls;
        this.pause  = pause;
    }

    /**
     * _more_
     */
    public void run() {
        for (int i = 0; i < 1000; i++) {
            for (String url : urls) {
                try {
                    url = url.trim();
                    if (url.length() == 0) {
                        continue;
                    }
                    url = url.replace("${server}", server);
                    System.err.println("reading:" + url);
                    String contents = IOUtil.readContents(url, getClass());
                    if (pause > 0) {
                        Misc.sleep(pause);
                    }
                } catch (Exception exc) {
                    System.err.println("Error:" + exc);
                    exc.printStackTrace();
                    return;
                }
                cnt++;
                if ((cnt % 10) == 0) {
                    long now = System.currentTimeMillis();
                    System.err.println("cnt:" + cnt + " time:"
                                       + (now - startTime) / 1000 + "s");
                }
            }
        }
    }

    /**
     * _more_
     *
     * @param msg _more_
     */
    private static void usage(String msg) {
        System.err.println("Error:" + msg);
        System.err.println(
            "Usage: java GsacTest -pause <milliseconds to sleep between url fetches> -server <server name> -threads <number of threads> -urls <url file>"
            + msg);
        System.exit(1);
    }

    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception On badness
     */
    public static void main(String[] args) throws Exception {
        String[] urls  = URLS;
        long     pause = 0;
        startTime = System.currentTimeMillis();
        String server     = "localhost:8080";
        int    numThreads = NUM_THREADS;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-server")) {
                if (i >= args.length - 1) {
                    usage("Missing -server argument");
                }
                server = args[++i];
            } else if (args[i].equals("-threads")) {
                if (i >= args.length - 1) {
                    usage("Missing -threads argument");
                }
                numThreads = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-pause")) {
                if (i >= args.length - 1) {
                    usage("Missing -pause argument");
                }
                pause = (long) Integer.parseInt(args[++i]);
            } else if (args[i].equals("-urls")) {
                if (i >= args.length - 1) {
                    usage("Missing -urls argument");
                }
                urls = IOUtil.readContents(args[++i],
                                           GsacTest.class).split("\n");
            } else {
                usage("Unknown argument: " + args[i]);
            }
        }



        //      numThreads = 1;
        System.err.println("Running with " + numThreads + " threads  pause="
                           + pause);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            executor.submit(new GsacTest(server, urls, pause));
        }
        executor.awaitTermination(1000000, TimeUnit.SECONDS);
        System.err.println("Done");
    }

}
