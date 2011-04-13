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

package org.gsac.gsl.output;



import java.util.ArrayList;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;







/**
 * Holds a collection of output handlers, e.g., all of the ones for sites or resources
 *
 */
public class OutputGroup {

    /** group id      */
    private String id;

    /** my outputs      */
    private List<GsacOutput> outputs = new ArrayList<GsacOutput>();

    /** map of output id to output handler       */
    private Hashtable<String, GsacOutput> map = new Hashtable<String,
        GsacOutput>();

    /**
     * ctor
     *
     * @param id group id
     */
    public OutputGroup(String id) {
        this.id = id;
    }

    public void addOutput(GsacOutput output) {
        map.put(output.getId(), output);
        outputs.add(output);
    }
    
    public GsacOutput getOutput(String id) {
        return map.get(id);
    }

    public List<GsacOutput> getOutputs() {
        return outputs;
    }

}

