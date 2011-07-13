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
import org.gsac.gsl.metadata.LinkMetadata;
import org.gsac.gsl.model.*;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.User;
import org.ramadda.repository.database.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.type.*;

import org.w3c.dom.*;

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
public class RamaddaGsacRepository extends GsacRepository {

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
    public String getPackagePath() {
        return "/org/gsac/ramadda";
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
            GsacSiteTypeHandler.CLASS_SITE, false, false);
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
     * @param response _more_
     *
     * @throws Exception _more_
     */
    public void handleSiteRequest(GsacRequest request, GsacResponse response)
            throws Exception {
        GsacSiteTypeHandler siteTypeHandler = getSiteTypeHandler();
        List<Clause>        clauses         = new ArrayList<Clause>();
        Clause              clause          = null;
        List<String>        tables          = new ArrayList();

        boolean             doJoin          = false;
        tables.add(GsacSiteTypeHandler.TABLE_GSACSITE);

        StringBuffer msgBuff = new StringBuffer();
        if (request.defined(GsacArgs.ARG_SITE_CODE)) {
            getResourceManager(GsacSite.CLASS_SITE).addStringSearch(request,
                             GsacArgs.ARG_SITE_CODE,
                             GsacArgs.ARG_SITE_CODE_SEARCHTYPE, msgBuff,
                             "Site Code",
                             GsacSiteTypeHandler.GSAC_COL_SITEID, clauses);
        }

        if (request.defined(GsacArgs.ARG_SITE_NAME)) {
            doJoin = true;
            getResourceManager(GsacSite.CLASS_SITE).addStringSearch(request,
                             GsacArgs.ARG_SITE_NAME,
                             GsacArgs.ARG_SITE_CODE_SEARCHTYPE, msgBuff,
                             "Site Name", Tables.ENTRIES.COL_DESCRIPTION,
                             clauses);
        }

        List<Clause> areaClauses = new ArrayList<Clause>();
        if (getResourceManager(GsacSite.CLASS_SITE).addBBOXSearchCriteria(
                request, areaClauses, Tables.ENTRIES.COL_SOUTH,
                Tables.ENTRIES.COL_WEST, msgBuff)) {
            clauses.addAll(areaClauses);
            doJoin = true;
        }


        if (doJoin) {
            tables.add(Tables.ENTRIES.NAME);
            clauses.add(Clause.join(Tables.ENTRIES.COL_ID,
                                    GsacSiteTypeHandler.GSAC_COL_ID));
        }
        System.err.println(clauses);
        getResourceManager(GsacSite.CLASS_SITE).setSearchCriteriaMessage(
            response, msgBuff);
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
                                    List<Clause> clauses, List<String> tables)
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
                                   GsacSiteTypeHandler.GSAC_COL_ID, tables,
                                   clause, null, -1)));
        for (String id : ids) {
            Entry entry = getRepository().getEntryManager().getEntry(null,
                              id);
            if (entry == null) {
                System.err.println("bad entry:" + id);
                continue;
            }
            Object[] values = entry.getValues();


            response.addResource(makeSite(entry));
        }
    }


    /**
     * _more_
     *
     *
     * @param type _more_
     * @param siteId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public GsacResource doGetObject(ResourceClass type, String siteId)
            throws Exception {
        if (type.equals(GsacSite.CLASS_SITE)) {
            Entry entry = getRepository().getEntryManager().getEntry(
                              getRepository().getTmpRequest(), siteId);
            if (entry == null) {
                return null;
            }
            return makeSite(entry);
        }
        return null;
    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public GsacSite makeSite(Entry entry) throws Exception {
        Object[] values = entry.getValues();
        GsacSite site = new GsacSite(entry.getId(),
                                     (String) entry.getValue(0, ""),
                                     entry.getName(), entry.getNorth(),
                                     entry.getWest(), entry.getAltitudeTop());
        String entryUrl = getRepository().absoluteUrl(
                              getRepository().getTmpRequest().entryUrl(
                                  getRepository().URL_ENTRY_SHOW, entry));
        site.addMetadata(new LinkMetadata(entryUrl, "RAMADDA Entry"));


        return site;
    }

    /**
     * _more_
     *
     * @param gsacRequest _more_
     * @param sb _more_
     * @param buffer _more_
     *
     * @return _more_
     */
    public Appendable decorateHtml(GsacRequest gsacRequest,
                                   Appendable buffer) {
        try {
            StringBuffer sb     = (StringBuffer) buffer;
            Result       result = new Result("GSAC", sb);
            Request request     =
                (Request) gsacRequest.getProperty("request");
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
