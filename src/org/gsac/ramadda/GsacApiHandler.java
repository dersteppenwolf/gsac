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



import org.w3c.dom.*;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.User;
import org.ramadda.repository.harvester.*;
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
import java.util.Properties;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GsacApiHandler extends RepositoryManager implements RequestHandler {

    /** _more_ */
    private GsacServlet gsacServlet;

    /** _more_ */
    private RamaddaGsacRepository gsacRepository;


    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     *
     * @throws Exception _more_
     */
    public GsacApiHandler(org.ramadda.repository.Repository repository, org.w3c.dom.Element node)
            throws Exception {
        super(repository);
        gsacRepository = new RamaddaGsacRepository(this);
        gsacServlet = new GsacServlet(gsacRepository, 8080, new Properties());
        gsacServlet.initServlet();
        gsacRepository.initServlet(gsacServlet);
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processGsacRequest(Request request) throws Exception {
        GsacRequest gsacRequest =
            new GsacRequest(gsacRepository, request.getHttpServletRequest(),
                            request.getHttpServletResponse());
        gsacRequest.putProperty("request", request);
        gsacServlet.doGet(gsacRequest);
        Result result = new Result();
        result.setNeedToWrite(false);
        return result;
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processGsacSiteRequest(Request request)
            throws Exception {
        return processGsacRequest(request);
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processGsacResourceRequest(Request request)
            throws Exception {
        return processGsacRequest(request);
    }
}
