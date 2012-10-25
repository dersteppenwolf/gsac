/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

package org.gsac.client;


import org.gsac.gsl.GsacArgs;
import org.gsac.gsl.GsacConstants;
import org.gsac.gsl.GsacRepository;
import org.gsac.gsl.util.GsacRepositoryInfo;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;

import ucar.unidata.util.StringUtil;


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

    /** _more_ */
    public static final String USER_AGENT = "gsac client v1.0";

    /** cmd line arg */
    public static final String ARG_SERVER = "server";

    /** cmd line arg */
    public static final String ARG_PROPERTIES = "properties";

    /** cmd line arg */
    public static final String ARG_URL = "url";

    /** cmd line arg */
    public static final String ARG_DOWNLOAD = "download";

    /** cmd line arg */
    public static final String ARG_KEEP_PATHS = "keep_paths";


    /** cmd line arg */
    public static final String ARG_HELP = "help";

    /** cmd line arg */
    public static final String ARG_QUERY = "query";

    /** cmd line arg */
    public static final String ARG_BBOX = "bbox";

    /** cmd line arg */
    public static final String ARG_OUT = "out";

    /** cmd line arg */
    public static final String ARG_INFO = "info";


    /** These are the command line arguments that are just for the client and do not get passed along on the request */
    public static final String[] clientArgs = { ARG_SERVER, ARG_QUERY,
                                                ARG_DOWNLOAD, ARG_KEEP_PATHS, ARG_OUT };



    /** for querying sites */
    public static final String QUERY_SITE = "site";

    /** for querying files */
    public static final String QUERY_FILE = "file";

    /** for command line arguments that specify a local file. See usage method */
    public static final String FILE_PREFIX = "file:";


    /** client properties */
    private Properties properties = new Properties();

    /** These are the repository url arguments we get from the command line */
    private List<String[]> queryArgs = new ArrayList<String[]>();

    /** the repository */
    private GsacRepository repository;

    /**
     * ctor
     *
     * @param args cmd line args
     *
     * @throws Exception On badness
     */
    public GsacClient(String[] args) throws Exception {
        /*
        final PrintStream oldErr = System.err;
          //This lets us see what code is writing to stderr
        System.setErr(new PrintStream(System.out){
                public void     println(String x) {
                    oldErr.println("ERR:" + x);
                    ucar.unidata.util.Misc.printStack("got it");
                }
                }); */

        //Make a dummy repository
        repository = new GsacRepository();
        repository.setUserAgent(USER_AGENT);

        //If the cmd line args are ok then process the query
        if (processArgs(args)) {
            processQuery();
        }
    }


    /**
     * Process the command line args
     *
     * @param args command line args
     *
     * @return If return is true then continue and process the query. If false then don't (e.g., the info request)
     *
     * @throws Exception On badness
     */
    private boolean processArgs(String[] args) throws Exception {

        loadDefaultProperties();
        //run through the cmd line args
        for (int i = 0; i < args.length; i++) {
            String argName = args[i];
            //            System.err.println("arg[" + i +"] = " + args[i]);

            //Strip off the "-"
            if (argName.startsWith("-")) {
                argName = argName.substring(1, argName.length());
            }

            if (argName.equals(ARG_INFO)) {
                if (getServer() == null) {
                    usage("-server needs to be specified");
                }
                handleInfoRequest();
                return false;
            }


            if (argName.equals(ARG_HELP)) {
                usage("");
            }


            //Shortcuts so the user can do  -site or -file instead of -query site or -query file
            if (argName.equals(QUERY_SITE)) {
                properties.put(ARG_QUERY, QUERY_SITE);
                continue;
            }

            if (argName.equals(QUERY_FILE)) {
                properties.put(ARG_QUERY, QUERY_FILE);
                continue;
            }

            //Spatial bounds are of the form:
            //-bbox west south east north
            if (argName.equals(ARG_BBOX)) {
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


            //Now any other arg is of the form:  -arg arg_value
            //so check for the correct number of args
            if (i + 1 >= args.length) {
                usage("Bad argument:" + args[i]);
            }

            //This is just a helper routine so the user can fetch a single url (in case they don't have wget for example)
            if (argName.equals(ARG_URL)) {
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


            if (argName.equals(ARG_PROPERTIES)) {
                properties.load(new FileInputStream(args[++i]));
                continue;
            }


            String  value   = args[++i];
            boolean foundIt = false;
            //If it is one of the client specific arguments then add it to the properties and continue
            for (String arg : clientArgs) {
                if (argName.equals(arg)) {
                    properties.put(argName, value);
                    foundIt = true;
                    break;
                }
            }
            if (foundIt) {
                continue;
            }

            //If the argument value is file: then read the file. Each line in the file
            //results in another url argumnet of the form: argName <line value>
            //See the usage for how this can be used
            if (value.startsWith(FILE_PREFIX)) {
                String contents = IOUtil.readContents(
                                      value.substring(FILE_PREFIX.length()),
                                      getClass());
                //Split and trim and exclude empty lines
                for (String line :
                        StringUtil.split(contents, "\n", true, true)) {
                    //Check for a comment
                    if (line.startsWith("#")) {
                        continue;
                    }
                    queryArgs.add(new String[] { argName, line });
                }
            } else {
                //Just a regular -arg <arg value>
                queryArgs.add(new String[] { argName, value });
            }
        }

        //Make sure they specified a server
        if (getServer() == null) {
            usage("-server needs to be specified");
        }
        return true;

    }


    /**
     * Load in the default property files
     *
     * @throws Exception On badness
     */
    private void loadDefaultProperties() throws Exception {
        //List of property files to load
        List<String> propertyFiles = new ArrayList<String>();

        //Load in the default properties
        propertyFiles.add("/org/gsac/client/gsac.properties");

        //Look around to where we are running from and load in any property files there
        //This lets the user have some default arguments that are loaded every time
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

        //Now process the  properties
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
    }


    /** _more_ */
    public static final String OUTPUT_FILE_URL = "file.url";
    public static final String OUTPUT_SITE_LOG_XML = "site.xmllog";


    /**
     * process the query
     *
     * @throws Exception On badness
     */
    private void processQuery() throws Exception {
        //Check if the user specifed a -download argument
        String download = getProperty(ARG_DOWNLOAD, null);
        if (download != null) {
            File downloadFile = new File(download);
            //Make sure it exists
            if ( !downloadFile.exists()) {
                usage("Download destination file does not exist:"
                      + downloadFile);
            }
            //Since we are in download mode we set the query to be a file query
            //and the output to be the URL listing
            properties.put(ARG_QUERY, QUERY_FILE);
            properties.put(GsacArgs.ARG_OUTPUT, OUTPUT_FILE_URL);
        }
        //Find out what we are querying and do the query
        String queryType = getProperty(ARG_QUERY, QUERY_SITE);
        System.err.println(queryType);
        if (queryType.equals(QUERY_SITE)) {
            processSiteQuery();
        } else if (queryType.equals(QUERY_FILE)) {
            processFileQuery();
        } else {
            usage("Unknown query:" + queryType);
        }
    }


    /**
     * process the query for sites. This adds the right kind of  site output, makes the URL and
     * fetches it
     *
     * @throws Exception On badness
     */
    private void processSiteQuery() throws Exception {
        List<String[]> args = new ArrayList<String[]>();
        args.addAll(queryArgs);
        boolean gotOutput = false;
        for (String[] pair : args) {
            if (pair[0].equals(GsacArgs.ARG_OUTPUT)) {
                gotOutput = true;
                break;
            }
        }
        if ( !gotOutput) {
            args.add(new String[] { GsacArgs.ARG_OUTPUT, "site.csv" });
        }
        String url = createUrl(GsacConstants.URL_SITE_SEARCH, args);
        System.err.println("Processing site query:");
        System.err.println(url);
        processUrl(url);
    }


    /**
     * process the query for files. This adds the right kind of  file output, makes the URL and
     * fetches it
     *
     * @throws Exception On badness
     */
    private void processFileQuery() throws Exception {
        List<String[]> args = new ArrayList<String[]>();
        args.addAll(queryArgs);
        //Add the output type. These types (e.g., "file.csv") are thos defined by the
        //gsac output handlers in org/gsac/gsl/output/file
        boolean gotOutput = false;
        for (String[] pair : args) {
            if (pair[0].equals(GsacArgs.ARG_OUTPUT)) {
                gotOutput = true;
                break;
            }
        }
        if ( !gotOutput) {
            String outputFromProperties =
                (String) properties.get(GsacArgs.ARG_OUTPUT);
            if (outputFromProperties != null) {
                gotOutput = true;
                args.add(new String[] { GsacArgs.ARG_OUTPUT,
                                        outputFromProperties });
            }
        }


        if ( !gotOutput) {
            args.add(new String[] { GsacArgs.ARG_OUTPUT, "file.csv" });
        }

        //Make the url
        String url = createUrl(GsacConstants.URL_FILE_SEARCH, args);
        System.err.println("Processing file query:");
        System.err.println(url);
        //process the url
        processUrl(url);
    }



    /**
     * fetch the contents from the url. This writes the output to stdout
     *
     * @param url url to fetch
     *
     * @throws Exception On badness
     */
    private void processUrl(String url) throws Exception {
        processUrl(url, getProperty(ARG_OUT, null));
    }



    /**
     * fetch the contents from the url. This writes the output to given file (if non null)
     * or to the stdout
     *
     * @param url url to fetch
     * @param file file to write to
     *
     * @throws Exception On badness
     */
    private void processUrl(String url, String file) throws Exception {

        //Get the contents
        String contents = fetchUrl(url);

        //Are we doing a download?
        String download = getProperty(ARG_DOWNLOAD, null);
        if (download != null) {
            File downloadFile = new File(download);
            doDownload(downloadFile, contents);
            return;
        }

        //If no file then print to stdout
        if (file == null) {
            System.out.print(contents);
            return;
        }

        //Write the contents to the file
        FileOutputStream out = new FileOutputStream(file);
        out.write(contents.getBytes());
        out.flush();
        out.close();
    }



    /**
     * If the user specified -download then the urls string is the (typically ftp) urls
     * we retrieved from the gsac server. Tokenize the url list and download them
     *
     * @param destDir Destination directory
     * @param urls urls
     *
     * @throws Exception On badness
     */
    public void doDownload(File destDir, String urls) throws Exception {
        System.err.println("Downloading urls to:" + destDir);
        final long[] totalSize = { 0 };
        final int[]  cnt       = { 0 };

        //TODO: split up the list and do the download in threads
        List<String> lines = StringUtil.split(urls, "\n", true, true);
        doDownload(destDir, lines, cnt, totalSize);
    }


    /**
     * Download the given urls into the given destination directory.
     * check for the ARG_KEEP_PATHS arg to see if we create subdirectories
     * based on the url path structure
     *
     * @param destDir destination directory
     * @param urls List of urls
     * @param cnt keeps track of the number of files
     * @param totalSize keeps track of the total size of the downloaded files
     *
     * @throws Exception On badness
     */
    public void doDownload(File destDir, List<String> urls, int[] cnt,
                           long[] totalSize)
            throws Exception {
        boolean keepPaths = getProperty(ARG_KEEP_PATHS,
                                        "true").equals("true");
        for (String line : urls) {
            //Skip comment line
            if (line.startsWith("#")) {
                continue;
            }
            //            System.err.println (line);
            String tail    = IOUtil.getFileTail(line);
            URL    url     = new URL(line);
            File   newDest = destDir;
            //Make the local directory if needed
            if (keepPaths) {
                String urlPath = url.getPath();
                if (urlPath.indexOf("..") >= 0) {
                    //Make sure there isn't anything funny here
                    urlPath = "";
                }
                List<String> toks = StringUtil.split(urlPath, "/", true,
                                        true);
                toks.remove(toks.size() - 1);
                String newPath = StringUtil.join("/", toks);
                newDest = new File(IOUtil.joinDir(newDest, newPath));
                //System.err.println ("newDest:" + newDest);
                IOUtil.makeDirRecursive(newDest);
            }

            File newFile = new File(IOUtil.joinDir(newDest, tail));

            //Skip it if we already have it.
            //Maybe make this a user option?
            if (newFile.exists()) {
                System.err.println("Skipping file:" + tail);
                continue;
            }

            //Fetch the URL
            URLConnection        connection = url.openConnection();
            InputStream          is         = connection.getInputStream();
            FileOutputStream     fos        = new FileOutputStream(newFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos, 8000);

            int                  numBytes   = IOUtil.writeTo(is, bos);
            IOUtil.close(bos);
            IOUtil.close(fos);
            //Update the size
            synchronized (totalSize) {
                totalSize[0] += numBytes;
                cnt[0]++;
                System.err.println("Downloaded " + cnt[0]
                                   + " files  Total size: " + totalSize[0]);
            }
        }
    }



    /**
     * Read the contents from the url
     *
     * @param url the url
     *
     * @return the contents
     *
     * @throws Exception On badness
     */
    private String fetchUrl(String url) throws Exception {
        URL           theUrl     = new URL(url);
        URLConnection connection = theUrl.openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);
        InputStream is       = connection.getInputStream();
        String      contents = readContents(is);
        is.close();
        return contents;
    }



    /**
     * read the contents from the input stream
     *
     * @param is input stream to read from
     *
     * @return contents
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
     * get the server
     *
     * @return the server
     */
    private String getServer() {
        return getProperty(ARG_SERVER, (String) null);
    }


    /**
     * Create the url with the given arguments and base path
     *
     * @param gsacPath base path (e.g.,  GsacConstants.URL_SITE_SEARCH)
     * @param args url arguments
     *
     * @return the url
     */
    private String createUrl(String gsacPath, List<String[]> args) {
        StringBuffer url = new StringBuffer(getServer());
        url.append(gsacPath);
        //TODO: encode the args
        if (args.size() > 0) {
            int cnt = 0;
            for (String[] pair : args) {
                if (cnt++ == 0) {
                    url.append("?");
                } else {
                    url.append("&");
                }
                url.append(HtmlUtil.arg(pair[0], pair[1], true));
            }
        }
        return url.toString();
    }





    /**
     * fetch the info from the server and print it out
     *
     * @throws Exception On badness
     */
    private void handleInfoRequest() throws Exception {
        GsacRepositoryInfo info =
            repository.retrieveRepositoryInfo(getServer());
        PrintWriter pw = new PrintWriter(System.out);
        pw.println(
            "Any of the site or file capabilities can be a command line argument (optionally  prepend a \"-\")");
        info.printDescription(pw);
        pw.flush();
    }


    /**
     * utility method to retrieve a property
     *
     * @param key property key
     * @param dflt default value
     *
     * @return property value if found. If not then the default value
     */
    private String getProperty(String key, String dflt) {
        String value = (String) properties.get(key);
        if (value == null) {
            return dflt;
        }
        return value;
    }




    /**
     * print  out the usage message and exit
     *
     * @param message message to print
     */
    private void usage(String message) {
        System.err.println(message);
        System.err.println("Usage: GsacClient");

        System.err.println("\t-properties <properties file to load>");

        System.err.println(
            "\t-" + ARG_SERVER
            + "  http://examplegsacrepository.edu/someprefixpath, e.g. http://facility.unavco.org/gsacws");
        /* System.err.println(" e.g. http://facility.unavco.org/gsacws"); */
        System.err.println(
            "\t-info  fetch and print to stdout the remote GSAC repository's information, including available arguments");
        System.err.println(
            "\t-" + ARG_DOWNLOAD
            + " <destination directory> Do a file search and download the files to the given directory");

        System.err.println(
            "\t-" + ARG_KEEP_PATHS
            + " <true|false> When doing the download, do we maintain the directory structure of the ftp urls?  Default is true");

        /* System.err.println("\t-" + ARG_QUERY + " site|file or: -" + QUERY_FILE + "|-" + QUERY_SITE); */
        System.err.println("\t-" + ARG_QUERY + " site  or -" + QUERY_SITE + " means do a site query (and add other arguments)." );
        System.err.println("\t-" + ARG_QUERY + " file  or -" + QUERY_FILE + " means do a file query (and add other arguments)." );

        System.err.println(
            "\t-" + ARG_OUT
            + " <outputfile>  Write the output to the specified file");

        System.err.println("\t-output  specify the format of the query results (such as "+OUTPUT_SITE_LOG_XML+" or "+OUTPUT_FILE_URL+ ")");

        System.err.println( "\t-url <url to fetch> <optional filename to write to> act like wget");

        System.err.println("\t After above gsaclient arguments you can add any number of query arguments, e.g.:");
        System.err.println("\t-site.code P12* ");
        System.err.println("\t-" + ARG_BBOX + " west-longi south-lati east-longi north-lati, such as -bbox -130.0 30.5 -125.0 33.0");
        System.err.println(
            "\tNote: for any of the arguments you can specify a file that contains extra arguments, e.g.:");
        System.err.println("\t\t-site.code " + FILE_PREFIX + "site_queries.txt");
        System.err.println(
            "\twhere sites_queries.txt contains site query codes and values, one per line.");
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
        LogUtil.setTestMode(true);
        try {
            GsacClient client = new GsacClient(args);
        } catch (Exception exc) {
            System.err.println("An error has occurred:" + exc);
            exc.printStackTrace();
        }
    }




}
