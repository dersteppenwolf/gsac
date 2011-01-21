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

package org.gsac.gsl.model;


import java.util.ArrayList;
import java.util.List;

import org.gsac.gsl.util.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class SiteType extends IdLabel {

    /**
     * _more_
     */
    public SiteType() {}

    public SiteType(IdLabel idLabel) {
        super(idLabel.getId(), idLabel.getName());
    }


    /**
     * _more_
     *
     * @param id _more_
     */
    public SiteType(String id) {
        super(id);
    }

    /**
     * _more_
     *
     * @param id _more_
     * @param name _more_
     */
    public SiteType(String id, String name) {
        super(id, name);
    }




    public static List<SiteType> convertList(List<IdLabel> ids) {
        List<SiteType> results =new ArrayList<SiteType>();
        if(ids==null) return results;
        for(IdLabel id: ids) {
            results.add(new SiteType(id));
        }
        return results;
    }


}
