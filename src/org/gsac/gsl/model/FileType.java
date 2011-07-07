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


import org.gsac.gsl.util.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class FileType extends IdLabel {

    /**
     * _more_
     */
    public FileType() {}

    /**
     * _more_
     *
     * @param idLabel _more_
     */
    public FileType(IdLabel idLabel) {
        super(idLabel.getId(), idLabel.getName());
    }

    /**
     * _more_
     *
     * @param id _more_
     */
    public FileType(String id) {
        super(id);
    }


    /**
     * _more_
     *
     * @param id _more_
     * @param name _more_
     */
    public FileType(String id, String name) {
        super(id, name);
    }


    /**
     * _more_
     *
     * @param ids _more_
     *
     * @return _more_
     */
    public static List<FileType> convertList(List<IdLabel> ids) {
        List<FileType> results = new ArrayList<FileType>();
        if (ids == null) {
            return results;
        }
        for (IdLabel id : ids) {
            results.add(new FileType(id));
        }
        return results;
    }

}
