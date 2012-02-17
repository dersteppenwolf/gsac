/*
 * $Id: SopacRepository.java 260 2011-10-11 16:58:15Z hankr $
 */

package edu.ucsd.sopac.projects.gsac.repository;


import org.gsac.gsl.*;
import org.gsac.gsl.database.*;
import org.gsac.gsl.model.*;
//import org.gsac.gsl.util.*;

import ucar.unidata.util.IOUtil;

import java.io.*;

//import javax.servlet.*;
import javax.servlet.http.*;


/**
 *
 * @author Jeff McWhirter mcwhirter@unavco.org
 */
public class SopacRepository extends GsacRepository implements GsacConstants {

    /** url path before the /gsacws/...  */
    //private String urlBase = "";

    /** html header */
    private String htmlHeader;

    /** html footer */
    private String htmlFooter;
    
    private boolean hasRun = false;

    /**
     * ctor
     */
    public SopacRepository() {
        try {
            initResources();
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    
    /**
     * initialize resources
     * CHANGME: Change the header.html and footer.html
     *
     * @throws Exception on badness
     */
    private void initResources() throws Exception {
        String packageName = getClass().getPackage().getName();
        packageName = packageName.replace(".","/");
        htmlHeader = ucar.unidata.util.IOUtil.readContents("/"+packageName+"/resources/header.html",
                                                           getClass());
        htmlFooter = ucar.unidata.util.IOUtil.readContents("/"+packageName +"/resources/footer.html",
                                                           getClass());
    }


    /**
     * Factory method to create the database manager
     *
     * @return database manager
     *
     * @throws Exception on badness
     */
    public GsacDatabaseManager doMakeDatabaseManager() throws Exception {
        SopacDatabaseManager dbm = new SopacDatabaseManager(this);
        dbm.init();
        // Uncomment this if you need to make Tables.java.  The dbm expects
        // a JNDI context, making it difficult to call from main()
        //if (!hasRun) {
        //    System.err.println( "Calling dbm.writeTables ..." );
        //	String packageName = dbm.getClass().getPackage().getName();
        //	dbm.writeTables(packageName);
        //	System.err.println( "Done." );
        //}
        return dbm;
    }


    public GsacResourceManager doMakeResourceManager(ResourceClass type) {
        if (type.equals(GsacSite.CLASS_SITE)) {
            return new SopacSiteManager(this);
        }
        if (type.equals(GsacFile.CLASS_FILE)) {
            return new SopacFileManager(this);
        }
        return null;
    }


    /*
     * CHANGEME Is this repository capable of certain things
     */
    public boolean isCapable(String arg) {
        //          Can we order searches for sites and resources
        /*
        if(arg.equals(ARG_SITE_SORT_VALUE)) return true;
        if(arg.equals(ARG_SITE_SORT_ORDER)) return true;
        if(arg.equals(ARG_FILE_SORT_VALUE)) return true;
        if(arg.equals(ARG_FILE_SORT_ORDER)) return true;
        */
        return super.isCapable(arg);
    }


    /**
     * get the html header
     *
     * @param request the request
     *
     * @return html header
     */
    public String getHtmlHeader(GsacRequest request) {
        return htmlHeader;
    }


    /**
     * get the html footer
     *
     * @param request the request
     *
     * @return html footer
     */
    public String getHtmlFooter(GsacRequest request) {
        return htmlFooter;
    }
    

    /**
     * 
     * Override the htdoc resources handler so sopac resources get delivered
     * Note that it requires that the build script copy the htdoc resources
     * to the resources location.
     * 
     * @param request - the custom Gsac request
     * 
     * @return void
     * 
     */
    public void handleHtdocsRequest(GsacRequest request) throws Exception {
    	
        String      uri         = request.getRequestURI();

        //System.err.println("uri: " + uri);
        
        // TODO: get the context uri so this is not hardcoded.
        int idx = uri.indexOf( "/gsacws/gsacapi/htdocs/sopac/" ); 
        if ( idx == 0) {
        	String path = uri.substring( "/gsacws/gsacapi/htdocs/sopac/".length() );
        	//System.err.println( "hit: " + path );
            String packageName = getClass().getPackage().getName();
            packageName = "/" + packageName.replace(".", "/");
            path = packageName + "/htdocs/" + path;
            //System.err.println( path );
        	
            InputStream inputStream = null;
            try {
                inputStream = getResourceInputStream(path);
            } catch (Exception exc) {}
            if (inputStream == null) {
                request.sendError(HttpServletResponse.SC_NOT_FOUND,
                                  "Could not find:" + uri);
                return;
            }
            /*
            if (uri.endsWith(".js") || uri.endsWith(".css") ) {
                String content = IOUtil.readContents(inputStream);
                inputStream.close();
                content     = replaceMacros(request, content);
                content     = replaceMacros(request, content);
                inputStream = new ByteArrayInputStream(content.getBytes());
            }
            */
            OutputStream outputStream = request.getOutputStream();
            IOUtil.writeTo(inputStream, outputStream);
            IOUtil.close(outputStream);
        } else {
            //        	super.handleHtdocsRequest(request);
        }
    	
    }
    
    
}
