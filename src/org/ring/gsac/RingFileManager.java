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

package org.ring.gsac;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;

/* CHANGEME - done for INGV - include datahase package for the GSAC installation. */
import org.ring.gsac.database.*;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.StringUtil;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


/**
 * Handles all of the resource related repository requests. The main entry point is {@link #handleRequest}
 * Look for the CHANGEME comments
 *
 * @author  Jeff McWhirter
 */
public class RingFileManager extends FileManager {

    public static final String TYPE_GNSS_OBSERVATION = "gnss.observation";

    private static ResourceType[] GNSS_FILE_TYPES = { new ResourceType(TYPE_GNSS_OBSERVATION, "GNSS - Observation") };

    public static final String[] GNSS_METADATA_COLUMNS = new String[] {
        Tables.SITI_GSAC.COL_ID_SITO,
        Tables.SITI_GSAC.COL_NOME_SITO,
        Tables.SITI_GSAC.COL_LUOGO,
        Tables.SITI_GSAC.COL_LATITUDINE,
        Tables.SITI_GSAC.COL_LONGITUDINE,
        Tables.SITI_GSAC.COL_IERS_DOMES_NUMBER,
        Tables.SITI_GSAC.COL_ID_RESPONSIBLE_AGENCY,
        Tables.SITI_GSAC.COL_ID_MONUMENTO,
        Tables.SITI_GSAC.COL_NAZIONE,
        Tables.SITI_GSAC.COL_REGIONE,
        Tables.SITI_GSAC.COL_AGENZIA,
    };

     /*
public static class SITI_GSAC extends Tables {
        public static final String NAME = "siti_gsac";

        public String getName() {return NAME;}
        public String getColumns() {return COLUMNS;}
        public static final String COL_ID_SITO =  NAME + ".id_sito";
        public static final String COL_NOME_SITO =  NAME + ".nome_sito";
        public static final String COL_ID_RETE =  NAME + ".id_rete";
        public static final String COL_DATA_DISMISSIONE =  NAME + ".data_dismissione";
        public static final String COL_GEOLOGIA =  NAME + ".geologia";
        public static final String COL_LUOGO =  NAME + ".luogo";
        public static final String COL_LATITUDINE =  NAME + ".latitudine";
        public static final String COL_LONGITUDINE =  NAME + ".longitudine";
        public static final String COL_QUOTA =  NAME + ".quota";
        public static final String COL_X =  NAME + ".x";
        public static final String COL_Y =  NAME + ".y";
        public static final String COL_Z =  NAME + ".z";
        public static final String COL_X_UTMED50 =  NAME + ".x_utmed50";
        public static final String COL_Y_UTMED50 =  NAME + ".y_utmed50";
        public static final String COL_TIPO_MATERIALIZZAZIONE =  NAME + ".tipo_materializzazione";
        public static final String COL_HEIGHT_OF_MONUMENT =  NAME + ".height_of_monument";
        public static final String COL_MONUMENT_FOUNDATION =  NAME + ".monument_foundation";
        public static final String COL_FOUNDATION_DEPTH =  NAME + ".foundation_depth";
        public static final String COL_MONUMENT_INSCRIPTION =  NAME + ".monument_inscription";
        public static final String COL_IERS_DOMES_NUMBER =  NAME + ".iers_domes_number";
        public static final String COL_BEDROCK_TYPES =  NAME + ".bedrock_types";
        public static final String COL_ID_ON_SITE_AGENCY =  NAME + ".id_on_site_agency";
        public static final String COL_ID_RESPONSIBLE_AGENCY =  NAME + ".id_responsible_agency";
        public static final String COL_ID_MONUMENTO =  NAME + ".id_monumento";
        public static final String COL_NAZIONE =  NAME + ".nazione";
        public static final String COL_REGIONE =  NAME + ".regione";
        public static final String COL_ATTIVO =  NAME + ".attivo";
        public static final String COL_NOTE =  NAME + ".note";
        public static final String COL_ID_DATA_CENTER =  NAME + ".id_data_center";
        public static final String COL_MONUMENTAZIONE_ESTERNA =  NAME + ".monumentazione_esterna";
        public static final String COL_AGENZIA =  NAME + ".agenzia";
        */

    private static ResourceType[][] ALL_FILE_TYPES = { GNSS_FILE_TYPES };

    /** _more_ */
    public static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone("UTC");

    /** _more_ */
    private SimpleDateFormat sdf;

    /** _more_ */
    private SimpleDateFormat yyyyMMDDSdf;




    /**
     * ctor
     *
     * @param repository the repository
     */
    public RingFileManager(RingRepository repository) {
        super(repository);

        // next 4 lines from cddis code; look is needed for ring?
        sdf         = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        yyyyMMDDSdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TIMEZONE_UTC);
        yyyyMMDDSdf.setTimeZone(TIMEZONE_UTC);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public List<Capability> doGetQueryCapabilities() {
        List<Capability> capabilities = new ArrayList<Capability>();

        // look - which are?   what does this do?
        addDefaultCapabilities(capabilities);

        // from cddis: look  is needed for ring?   what does this do?
        capabilities.addAll(getSiteManager().doGetQueryCapabilities());

        return capabilities;
    }

    /**
     * CHANGEME
     * handle the search request
     *
     * @param request The request
     * @param response The response
     *
     * @throws Exception on badness
     */
    public void handleRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        //Some example code

        //The msgBuff holds the html that describes what is being searched for
        StringBuffer msgBuff = new StringBuffer();
        if (request.defined(ARG_FILESIZE_MIN)) {
            int size = request.get(ARG_FILESIZE_MIN, 0);
            appendSearchCriteria(msgBuff, "Filesize&gt;=",
                                 "" + request.get(ARG_FILESIZE_MIN, 0));
        }

        if (request.defined(ARG_FILESIZE_MAX)) {
            int size = request.get(ARG_FILESIZE_MAX, 0);
            appendSearchCriteria(msgBuff, "Filesize&lt;=",
                                 "" + request.get(ARG_FILESIZE_MAX, 0));
        }

        if (request.defined(ARG_FILE_TYPE)) {
            List<String> types =
                (List<String>) request.getList(ARG_FILE_TYPE);
            addSearchCriteria(msgBuff, "Resource Type", types, ARG_FILE_TYPE);
        }

        Date[] publishDateRange =
            request.getDateRange(ARG_FILE_PUBLISHDATE_FROM,
                                 ARG_FILE_PUBLISHDATE_TO, null, null);

        if (publishDateRange[0] != null) {
            appendSearchCriteria(msgBuff, "Publish date&gt;=",
                                 "" + format(publishDateRange[0]));
        }

        if (publishDateRange[1] != null) {
            appendSearchCriteria(msgBuff, "Publish date&lt;=",
                                 "" + format(publishDateRange[1]));
        }


        Date[] dataDateRange = request.getDateRange(ARG_FILE_DATADATE_FROM,
                                   ARG_FILE_DATADATE_TO, null, null);
        //        System.err.println ("date:" + dateRange[0] +" " + dateRange[1]);


        if (dataDateRange[0] != null) {
            appendSearchCriteria(msgBuff, "Publish date&gt;=",
                                 "" + format(dataDateRange[0]));
        }

        if (dataDateRange[1] != null) {
            appendSearchCriteria(msgBuff, "Publish date&lt;=",
                                 "" + format(dataDateRange[1]));
        }


        //find and create the files
        /**
         *   e.g.:
         * GsacResource site = theSiteForThisFile; may be null
         * String type = someType;
         * GsacFile resource = new GsacFile(resourceId,
         *                           new FileInfo(filePath, fileSize, md5),
         *                           site,
         *                           publishTime, fromTime, toTime,
         *                           toResourceType(type));
         *
         *                           response.addResource(resource);
         */

        //long t2 = System.currentTimeMillis();
        //System.err.println("read " + cnt + " resources in " + (t2 - t1) + "ms");

        setSearchCriteriaMessage(response, msgBuff);
    }



    /**
     * CHANGEME
     * This takes the resource id that is used to identify files and
     * creates a GsacFile object
     *
     * @param resourceId file id
     *
     * @return GsacFile
     *
     * @throws Exception On badness
     */
    public GsacResource getResource(String resourceId) throws Exception {
        return null;
    }


    /**
     * Create the list of resource types that are shown to the user. This is
     * called by the getDefaultCapabilities  look which is where?
     *
     * @return resource types
     */
    public List<ResourceType> doGetResourceTypes() {
        List<ResourceType> resourceTypes = new ArrayList<ResourceType>();

        //resourceTypes.add(new ResourceType("rinex", "RINEX Files"));
        //resourceTypes.add(new ResourceType("qc", "QC Files"));

        return resourceTypes;
    }


    /**
     * helper method
     *
     * @return sitemanager
     */
    public RingSiteManager getSiteManager() {
        return (RingSiteManager) getRepository().getResourceManager(
            GsacSite.CLASS_SITE);
    }

}
