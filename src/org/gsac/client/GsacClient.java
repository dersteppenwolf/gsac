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

package org.gsac.client;


import org.gsac.gsl.GsacArgs;
import org.gsac.gsl.GsacConstants;
import org.gsac.gsl.GsacRepository;
import org.gsac.gsl.util.GsacRepositoryInfo;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.HtmlUtil;


import java.io.*;

import java.lang.management.*;

import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * client for accessing gsac repositories
 *
 *
 * @author Jeff McWhirter mcwhirter@unavco.org
 */

public class GsacClient implements GsacConstants {

    /** _more_          */
    public static final String ARG_SERVER = "server";

    public static final String ARG_DOWNLOAD = "download";

    public static final String ARG_KEEP_PATHS = "keep_paths";


    public static final String ARG_HELP = "help";

    /** _more_          */
    public static final String ARG_QUERY = "query";

    /** _more_          */
    public static final String ARG_BBOX = "bbox";

    /** _more_          */
    public static final String ARG_OUTPUT = "output";

    public static final String[]myArgs  = {
        ARG_OUTPUT,
        ARG_SERVER,
        ARG_QUERY,
        ARG_DOWNLOAD,
        ARG_KEEP_PATHS
    };

    /** _more_          */
    public static final String ARG_FILE = "file";

    /** _more_          */
    public static final String QUERY_SITE = "site";

    /** _more_          */
    public static final String QUERY_RESOURCE = "resource";

    /** _more_          */
    public static final String OUTPUT_CSV = "csv";

    /** _more_          */
    public static final String OUTPUT_XML = "xml";

    /** _more_          */
    public static final String OUTPUT_URL = "url";

    /** _more_          */
    public static final String OUTPUT_JSON = "json";

    /** _more_          */
    public static final String ARG_INFO = "-info";

    /** _more_          */
    public static final String ARG_URL = "-url";

    /** _more_          */
    private Properties properties = new Properties();

    /** _more_          */
    private List<String[]> queryArgs = new ArrayList<String[]>();

    /** _more_          */
    private GsacRepository repository;

    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception On badness
     */
    public GsacClient(String[] args) throws Exception {
        final PrintStream oldErr = System.err;                                              
	/*
        System.setErr(new PrintStream(System.out){                                          
                public void     println(String x) {                                         
                    oldErr.println("ERR:" + x);                                             
		    ucar.unidata.util.Misc.printStack("got it");                                          
                }                                                                           
		}); */

        repository = new GsacRepository();
        if (processArgs(args)) {
            processQuery();
        }
    }

    /**
     * _more_
     *
     * @param url _more_
     *
     * @throws Exception On badness
     */
    private void processUrl(String url) throws Exception {
        processUrl(url, getProperty(ARG_FILE, null));
    }

    public void doDownload(File destDir, String urls) throws Exception {
        System.err.println("Downloading urls to:" + destDir);
        final long [] totalSize={0};
        final int [] cnt={0};
        //TODO: split up the list and do the download in threads
        List<String> lines = StringUtil.split(urls,"\n", true, true);
        doDownload(destDir, lines, cnt, totalSize);
    }


    public void doDownload(File destDir, List<String> lines, int[]cnt, long[]totalSize) throws Exception {
        boolean keepPaths = getProperty(ARG_KEEP_PATHS,"true").equals("true");
        for(String line: lines) {
            if(line.startsWith("#")) continue;
            //            System.err.println (line);
            String        tail       = IOUtil.getFileTail(line);
            URL           url        = new URL(line);
            File          newDest    = destDir;
            if(keepPaths) {
                String urlPath = url.getPath();
                if (urlPath.indexOf("..") >= 0) {
                    //Make sure there isn't anything funny here
                    urlPath = "";
                }
                List<String> toks = StringUtil.split(urlPath,
                                                     "/", true, true);
                toks.remove(toks.size() - 1);
                String newPath = StringUtil.join("/", toks);
                newDest = new File(IOUtil.joinDir(newDest,
                                                  newPath));
                //System.err.println ("newDest:" + newDest);
                IOUtil.makeDirRecursive(newDest);
            }

            File newFile = new File(IOUtil.joinDir(newDest,
                                                   tail));
            if (newFile.exists()) {
                System.err.println("Skipping file:" + tail);
                continue;
            }
            //            if(true)continue;

            URLConnection connection = url.openConnection();
            InputStream   is = connection.getInputStream();
            int numBytes = IOUtil.writeTo(
                                          is,
                                          new BufferedOutputStream(
                                                                   new FileOutputStream(
                                                                                        newFile), 8000));
            synchronized (totalSize) {
                totalSize[0] += numBytes;
                cnt[0]++;
                System.err.println("Downloaded " + cnt[0]
                                   + " files  Total size: "
                                   + totalSize[0]);
            }
        }
    }


    /**
     * _more_
     *
     * @param url _more_
     * @param file _more_
     *
     * @throws Exception On badness
     */
    private void processUrl(String url, String file) throws Exception {
        String contents = fetchUrl(url);

        String download = getProperty(ARG_DOWNLOAD,null);
        if(download!=null) {
            File downloadFile = new File(download);
            doDownload(downloadFile, contents);
            return;
        }

        if (file == null) {
            System.out.print(contents);
            return;
        }
        FileOutputStream out = new FileOutputStream(file);
        out.write(contents.getBytes());
        out.flush();
        out.close();
    }


    /**
     * _more_
     *
     * @param url _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    private String fetchUrl(String url) throws Exception {
        URL           theUrl     = new URL(url);
        URLConnection connection = theUrl.openConnection();
        InputStream   is         = connection.getInputStream();
        String        contents   = readContents(is);
        is.close();
        return contents;
    }



    /**
     * _more_
     *
     * @param is _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    private String readContents(InputStream is) throws Exception {
        StringBuilder sb = new StringBuilder();
        String        line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(is,
                                    "UTF-8"));
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    /**
     * _more_
     *
     * @throws Exception On badness
     */
    private void processQuery() throws Exception {
        String download = getProperty(ARG_DOWNLOAD,null);
        if(download!=null) {
            File downloadFile = new File(download);
            if(!downloadFile.exists()) {
                usage("Download destination file does not exist:" + downloadFile);
            }
            properties.put(ARG_QUERY, QUERY_RESOURCE);
            properties.put(ARG_OUTPUT, OUTPUT_URL);
        }
        String query = getProperty(ARG_QUERY, QUERY_SITE);
	System.err.println(query);
        if (query.equals(QUERY_SITE)) {
            processSiteQuery();
        } else if (query.equals(QUERY_RESOURCE)) {
            processResourceQuery();
        } else {
            usage("Unknown query:" + query);
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    private String getServer() {
        return getProperty(ARG_SERVER, (String) null);
    }


    /**
     * _more_
     *
     * @param gsacPath _more_
     *
     * @return _more_
     */
    private String getUrl(String gsacPath) {
        return getUrl(gsacPath, new ArrayList<String[]>());
    }


    /**
     * _more_
     *
     * @param gsacPath _more_
     * @param args _more_
     *
     * @return _more_
     */
    private String getUrl(String gsacPath, List<String[]> args) {
        StringBuffer url = new StringBuffer(getServer());
        url.append(gsacPath);
        //TODO: encode the args
        if (args.size() > 0) {
	    int cnt = 0;
            for (String[] pair : args) {
		if(cnt++==0)
		    url.append("?");
		else
		    url.append("&");
                url.append(HtmlUtil.arg(pair[0],pair[1],true));
            }
        }
        return url.toString();
    }


    /**
     * _more_
     *
     * @param args _more_
     * @param arg _more_
     */
    private void addArg(List<String[]> args, String arg) {
        String value = getProperty(arg, (String) null);
        if (value != null) {
            args.add(new String[] { arg, value });
        }
    }

    /**
     * _more_
     *
     * @param args _more_
     */
    private void getSiteArgs(List<String[]> args) {}


    /**
     * _more_
     *
     * @throws Exception On badness
     */
    private void processSiteQuery() throws Exception {
        List<String[]> args = new ArrayList<String[]>();
        args.addAll(queryArgs);
        String output = getProperty(ARG_OUTPUT, OUTPUT_CSV);
        if (output.equals(OUTPUT_CSV)) {
            args.add(new String[] { GsacArgs.ARG_OUTPUT, "site.csv" });
        } else if (output.equals(OUTPUT_XML)) {
            args.add(new String[] { GsacArgs.ARG_OUTPUT, "site.xml" });
        } else if (output.equals(OUTPUT_JSON)) {
            args.add(new String[] { GsacArgs.ARG_OUTPUT, "site.json" });
        } else {
            usage("Unknown site output:" + output);
        }
        getSiteArgs(args);
        String url = getUrl(GsacConstants.URL_SITE_SEARCH, args);
        System.err.println("Processing site query:");
        System.err.println(url);
        processUrl(url);
    }

    /**
     * _more_
     *
     * @param args _more_
     */
    private void getResourceArgs(List<String[]> args) {}

    /**
     * _more_
     *
     * @throws Exception On badness
     */
    private void processResourceQuery() throws Exception {
        List<String[]> args = new ArrayList<String[]>();
        args.addAll(queryArgs);
        String output = getProperty(ARG_OUTPUT, OUTPUT_CSV);
        if (output.equals(OUTPUT_CSV)) {
            args.add(new String[] { GsacArgs.ARG_OUTPUT, "resource.csv" });
        } else if (output.equals(OUTPUT_XML)) {
            args.add(new String[] { GsacArgs.ARG_OUTPUT, "resource.xml" });
        } else if (output.equals(OUTPUT_URL)) {
            args.add(new String[] { GsacArgs.ARG_OUTPUT, "resource.url" });
        } else if (output.equals(OUTPUT_JSON)) {
            args.add(new String[] { GsacArgs.ARG_OUTPUT, "resource.json" });
        } else {
            usage("Unknown resource output:" + output);
        }
        getSiteArgs(args);
        getResourceArgs(args);
        String url = getUrl(GsacConstants.URL_RESOURCE_SEARCH, args);
        System.err.println("Processing resource query:");
        System.err.println(url);
        processUrl(url);
    }




    /**
     * _more_
     *
     * @param key _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    private String getProperty(String key, String dflt) {
        String value = (String) properties.get(key);
        if (value == null) {
            return dflt;
        }
        return value;
    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @return _more_
     *
     * @throws Exception On badness
     */
    private boolean processArgs(String[] args) throws Exception {

        List<String> propertyFiles = new ArrayList<String>();
        propertyFiles.add("/org/gsac/client/gsac.properties");

        File path =
            new File(getClass().getProtectionDomain().getCodeSource()
                .getLocation().getPath());

        if (path.exists() && !path.isDirectory()
                && path.getName().endsWith(".jar")) {
            File parentDir = path.getParentFile();
            //look to see if we are running from the install dir
            if (parentDir.getName().equals("lib")) {
                parentDir = parentDir.getParentFile();
            }
            if (parentDir != null) {
                propertyFiles.add(parentDir + File.separator
                                  + "gsac.properties");
            }
        }

        //TODO: Should we look for a gsac.properties file in the working dir?

        for (String propertyFile : propertyFiles) {
            InputStream is = getClass().getResourceAsStream(propertyFile);
            if (is == null) {
                if (new File(propertyFile).exists()) {
                    is = new FileInputStream(propertyFile);
                }
            }
            //            System.err.println("property file:" + propertyFile +"  OK?" + (is!=null));
            if (is != null) {
                properties.load(is);
                is.close();
            }
        }


        for (int i = 0; i < args.length; i++) {
            String name = args[i];
            if (name.equals(ARG_INFO)) {
                if (getServer() == null) {
                    usage("-server needs to be specified");
                }
                handleInfoRequest();
                return false;
            }


            if (name.startsWith("-")) {
                name = name.substring(1, name.length());
            }

	    if(name.equals(ARG_HELP)) {
		usage("");
	    }


            if (name.equals(QUERY_SITE)) {
                properties.put(ARG_QUERY, QUERY_SITE);
                continue;
            }

            if (name.equals(QUERY_RESOURCE)) {
                properties.put(ARG_QUERY, QUERY_RESOURCE);
                continue;
            }

            if (name.equals(ARG_BBOX)) {
                if (i + 4 >= args.length) {
                    usage("Bad arguments for " + ARG_BBOX);
                }
                queryArgs.add(new String[] { GsacArgs.ARG_WEST,
                                             args[i + 1] });
                queryArgs.add(new String[] { GsacArgs.ARG_SOUTH,
                                             args[i + 2] });
                queryArgs.add(new String[] { GsacArgs.ARG_EAST,
                                             args[i + 3] });
                queryArgs.add(new String[] { GsacArgs.ARG_NORTH,
                                             args[i + 4] });
                i += 4;
                continue;
            }


            if (i + 1 >= args.length) {
                usage("Bad arguments:" + args[i]);
            }

            if (name.equals("properties")) {
                properties.load(new FileInputStream(args[++i]));
            }


            if (name.equals("url")) {
                String url = args[++i];
                String filename;
                if (args.length > i) {
                    filename = args[++i];
                } else {
                    filename = IOUtil.getFileTail(url);
                }
                System.err.println("Writing url to:" + filename);
                processUrl(url, filename);
                return false;
            }


            String value = args[++i];
            boolean foundIt = false;
            for (String arg : myArgs) {
                if (name.equals(arg)) {
                    properties.put(name, value);
                    foundIt = true;
                    break;
                }
            }
            if (foundIt) {
                continue;
            }

            if(value.startsWith("file:")) {
                String contents = IOUtil.readContents(value.substring("file:".length()), getClass());
                for(String line: StringUtil.split(contents,"\n",true,true)) {
                    if(line.startsWith("#")) continue;
                    queryArgs.add(new String[] { name, line});
                }
            } else {
                queryArgs.add(new String[] { name, value});
            }
        }

        if (getServer() == null) {
            usage("-server needs to be specified");
        }
        return true;


    }

    /**
     * _more_
     *
     * @throws Exception On badness
     */
    private void handleInfoRequest() throws Exception {
        GsacRepositoryInfo info =
            repository.retrieveRepositoryInfo(getServer());
        PrintWriter pw = new PrintWriter(System.out);
        pw.println(
            "Any of the site or resource capabilities can be a command line argument (optionally  prepend a \"-\")");
        info.printDescription(pw);
        pw.flush();
    }


    /**
     * _more_
     *
     * @param message _more_
     */
    private void usage(String message) {
        System.err.println(message);
        System.err.println("Usage: GsacClient");

        System.err.println("\t-properties <properties file to load>");
        System.err.println(
            "\t-url <url to fetch> <optional filename to write to> act like wget");
        System.err.println(
            "\t-" + ARG_SERVER
            + "  http://examplegsacrepository.edu/someprefixpath");
        System.err.println(
            "\te.g.: http://facdev.unavco.org/gsacws");
        System.err.println(
            "\t-info  fetch and print to stdout the repository information includings available arguments");
        System.err.println("\t-" + ARG_DOWNLOAD +" <destination directory> Do a resource search and download the files to the given directory");

        System.err.println("\t-" + ARG_KEEP_PATHS+" <true|false> When doing the download do we maintain the directory structure of the ftp urls. Default is true");



        System.err.println("\t-" + ARG_QUERY + " site|resource or: -" + QUERY_RESOURCE +"|-" + QUERY_SITE);
        System.err.println("\t-" + ARG_OUTPUT + " csv|xml|url");
        System.err.println("\t-" + ARG_FILE + " outputfile");
        System.err.println("\tany number of query arguments, e.g.:");
        System.err.println("\t-site.code \"P12*\"");
        System.err.println("\t-" + ARG_BBOX + " west south east north");
        System.err.println("\tnote: for any of the arguments you can specify a file that contains the argument values, e.g.:");
        System.err.println("\t\t-site.code file:sites.txt");
        System.err.println("\tWhere sites.txt contains site codes, one per line");
        System.exit(1);
    }


    /**
     * main
     *
     * @param args args
     *
     * @throws Exception On badness
     */
    public static void main(String[] args) throws Exception {
        try {
            GsacClient client = new GsacClient(args);
        } catch (Exception exc) {
            System.err.println("An error has occurred:" + exc);
            exc.printStackTrace();
        }
    }




}
