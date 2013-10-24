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

package org.prototype.gsac;

import ucar.unidata.util.IOUtil;

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Tester
 */
public class PrototypeTest implements Runnable {
    //Change these to point to your server sites

    static String[]urls = {
        "http://${server}/gsacws/site/search/sites.kml?output=site.kml&limit=1000&site.code=p12*",
        "http://${server}/gsacws/site/search/sites.csv?output=site.csv&limit=1000&site.code=p12*",
        "http://${server}/gsacws/site/search?limit=1000&site.code=p*"};

    static int  cnt = 0;

    static int NUM_THREADS = 10;

    private String server;

    private static long startTime;

    public PrototypeTest(String server) {
        this.server = server;
    }

    public void run() {
        for(int i=0;i<1000;i++) {
            for(String url: urls) {
                try {
                    url = url.replace("${server}", server);
                    IOUtil.readContents(url, PrototypeTest.class);
                } catch(Exception exc) {
                    System.err.println ("Error:" + exc);
                    return;
                }
                cnt++;
                if((cnt%10) == 0) {
                    long now = System.currentTimeMillis();
                    System.err.println ("cnt:" + cnt +" time:" + (now-startTime)/1000 + "s");
                }
            }
        }
    }

    private static void usage(String msg) {
        System.err.println("Error:" + msg);
        System.err.println("Usage: java UnavcoTest -server servername -threads number_of_threads" + msg);
        System.exit(1);
    }


    public static void main(String[] args) throws Exception {
        startTime = System.currentTimeMillis();
        String server = "localhost:8080";

        int numThreads = NUM_THREADS;
        for(int i=0;i<args.length;i++) {
            if(args[i].equals("-server")) {
                if(i>=args.length-1) usage("Missing -server argument");
                server = args[++i];
            } else if(args[i].equals("-threads")) {
                if(i>=args.length-1) usage("Missing -threads argument");
                numThreads = Integer.parseInt(args[++i]);
            } else {
                usage("Unknown argument: " + args[i]);
            }
        }


        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        for(int i=0;i<NUM_THREADS;i++) {
            executor.submit(new PrototypeTest(server));
        }
        executor.awaitTermination(1000000, TimeUnit.SECONDS);
        System.err.println ("Done");
    }

}
