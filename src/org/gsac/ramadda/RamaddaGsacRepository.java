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

package org.gsac.ramadda;


import org.gsac.gsl.*;
import org.gsac.gsl.model.*;

import org.w3c.dom.*;


import ucar.unidata.repository.*;
import ucar.unidata.repository.auth.User;
import ucar.unidata.repository.database.*;
import ucar.unidata.repository.harvester.*;
import ucar.unidata.repository.type.*;
import ucar.unidata.sql.Clause;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;




import java.io.File;



import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class RamaddaGsacRepository extends GsacRepositoryImpl {

    /** _more_ */
    GsacApiHandler apiHandler;

    /**
     * _more_
     *
     * @param apiHandler _more_
     *
     * @throws Exception _more_
     */
    public RamaddaGsacRepository(GsacApiHandler apiHandler) throws Exception {
        this.apiHandler = apiHandler;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getUrlBase() {
        return getRepository().getUrlBase();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Repository getRepository() {
        return apiHandler.getRepository();
    }


    /**
     * _more_
     *
     * @param message _more_
     * @param exc _more_
     */
    public void logError(String message, Exception exc) {
        getRepository().getLogManager().logError(message, exc);
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private GsacSiteTypeHandler getSiteTypeHandler() throws Exception {
        return (GsacSiteTypeHandler) getRepository().getTypeHandler(
            GsacSiteTypeHandler.TYPE_GSACSITE, false, false);
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param siteId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public GsacSite getSiteInner(GsacRequest request, String siteId)
            throws Exception {
        GsacResponse response = new GsacResponse();
        List<Clause> clauses  = new ArrayList<Clause>();
        clauses.add(Clause.eq(GsacSiteTypeHandler.GSAC_COL_ID, siteId));
        processSiteRequest(response, clauses, null);
        if (response.getSites().size() == 0) {
            return null;
        }
        return response.getSites().get(0);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param resourceId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public GsacResource getResource(GsacRequest request, String resourceId)
            throws Exception {
        return null;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param response _more_
     *
     * @throws Exception _more_
     */
    public void handleSiteRequest(GsacRequest request,
                                     GsacResponse response)
            throws Exception {
        GsacSiteTypeHandler siteTypeHandler = getSiteTypeHandler();
        List<Clause>           clauses            = new ArrayList<Clause>();
        Clause                 clause             = null;
        List<String>           tables             = new ArrayList();

        boolean                doJoin             = false;
        tables.add(GsacSiteTypeHandler.TABLE_GSACSITE);

        if (request.defined(ARG_SITEID)) {
            clauses.add(Clause.eq(GsacSiteTypeHandler.GSAC_COL_ID,
                                  request.get(ARG_SITEID, (String) null)));
        }

        if (request.defined(ARG_NORTH) || request.defined(ARG_SOUTH)
                || request.defined(ARG_EAST) || request.defined(ARG_WEST)) {
            doJoin = true;
            List<Clause> areaClauses = new ArrayList<Clause>();
            if (request.defined(ARG_NORTH)) {
                areaClauses.add(Clause.le(Tables.ENTRIES.COL_SOUTH,
                                          request.get(ARG_NORTH, 0.0)));
            }
            if (request.defined(ARG_SOUTH)) {
                areaClauses.add(Clause.ge(Tables.ENTRIES.COL_SOUTH,
                                          request.get(ARG_SOUTH, 0.0)));
            }
            if (request.defined(ARG_WEST)) {
                areaClauses.add(Clause.ge(Tables.ENTRIES.COL_WEST,
                                          request.get(ARG_WEST, 0.0)));
            }
            if (request.defined(ARG_EAST)) {
                areaClauses.add(Clause.le(Tables.ENTRIES.COL_WEST,
                                          request.get(ARG_EAST, 0.0)));
            }
            clauses.addAll(areaClauses);
        }


        if (doJoin) {
            tables.add(Tables.ENTRIES.NAME);
            clauses.add(Clause.join(Tables.ENTRIES.COL_ID,
                                    GsacSiteTypeHandler.GSAC_COL_ID));
        }
        processSiteRequest(response, clauses, tables);
    }

    /**
     * _more_
     *
     * @param response _more_
     * @param clauses _more_
     * @param tables _more_
     *
     * @throws Exception _more_
     */
    private void processSiteRequest(GsacResponse response,
                                       List<Clause> clauses,
                                       List<String> tables)
            throws Exception {
        if (tables == null) {
            tables = new ArrayList<String>();
            tables.add(GsacSiteTypeHandler.TABLE_GSACSITE);
        }
        Clause clause = null;
        if (clauses.size() > 0) {
            clause = Clause.and(clauses);
        }
        String[] ids = SqlUtil.readString(
                           getRepository().getDatabaseManager().getIterator(
                               getRepository().getDatabaseManager().select(
                                   GsacSiteTypeHandler.GSAC_COL_ID,
                                   tables, clause, null, -1)));
        for (String id : ids) {
            Entry entry = getRepository().getEntryManager().getEntry(null,
                              id);
            Object[] values = entry.getValues();
            response.addSite(new GsacSite(entry.getId(),
                    (String) values[0], entry.getName(), entry.getNorth(),
                    entry.getWest(), entry.getAltitudeTop()));
        }
    }


    /**
     * _more_
     *
     * @param gsacRequest _more_
     * @param sb _more_
     *
     * @return _more_
     */
    public StringBuffer decorateHtml(GsacRequest gsacRequest,
                                     StringBuffer sb) {
        try {
            Result  result  = new Result("GSAC", sb);
            Request request = (Request) gsacRequest.getProperty("request");
            if (request == null) {
                request = getRepository().getTmpRequest();
            }
            result.putProperty(Repository.PROP_NAVLINKS,
                               getRepository().getNavLinks(request));
            getRepository().decorateResult(request, result);
            return new StringBuffer(new String(result.getContent()));
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public String getHtmlHeader(GsacRequest request) {
        return "";
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public String getHtmlFooter(GsacRequest request) {
        return "";
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param response _more_
     *
     * @throws Exception _more_
     */
    public void handleResourceRequest(GsacRequest request,
                                      GsacResponse response)
            throws Exception {}

}
