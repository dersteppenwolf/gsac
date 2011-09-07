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


import org.gsac.gsl.GsacArgs;
import org.gsac.gsl.GsacConstants;


import org.gsac.gsl.util.*;

import ucar.unidata.util.IOUtil;

import java.util.Date;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Wed, May 19, '10
 * @author         Enter your name here...
 */
public class GsacFile extends GsacResource {

    /** _more_ */
    public static final ResourceClass CLASS_FILE = new ResourceClass("file");

    /** _more_ */
    private FileInfo fileInfo;


    /**
     * ctor
     */
    public GsacFile() {}



    /**
     * _more_
     *
     * @param repositoryId _more_
     * @param fileInfo _more_
     * @param relatedResource _more_
     * @param publishTime _more_
     * @param type _more_
     */
    public GsacFile(String repositoryId, FileInfo fileInfo,
                    GsacResource relatedResource, Date publishTime,
                    ResourceType type) {
        this(repositoryId, fileInfo, relatedResource, publishTime,
             publishTime, publishTime, type);
    }


    /**
     * _more_
     *
     * @param repositoryId _more_
     * @param fileInfo _more_
     * @param publishTime _more_
     * @param type _more_
     */
    public GsacFile(String repositoryId, FileInfo fileInfo, Date publishTime,
                    ResourceType type) {
        this(repositoryId, fileInfo, null, publishTime, publishTime,
             publishTime, type);
    }



    /**
     * ctor
     *
     * @param repositoryId _more_
     * @param fileInfo _more_
     * @param relatedResource _more_
     * @param startTime _more_
     * @param endTime _more_
     * @param type _more_
     */
    public GsacFile(String repositoryId, FileInfo fileInfo,
                    GsacResource relatedResource, Date startTime,
                    Date endTime, ResourceType type) {
        this(repositoryId, fileInfo, relatedResource, startTime, startTime,
             endTime, type);
    }



    /**
     * _more_
     *
     * @param repositoryId _more_
     * @param fileInfo _more_
     * @param relatedResource _more_
     * @param publishTime _more_
     * @param startTime _more_
     * @param endTime _more_
     * @param type _more_
     */
    public GsacFile(String repositoryId, FileInfo fileInfo,
                    GsacResource relatedResource, Date publishTime,
                    Date startTime, Date endTime, ResourceType type) {
        super(repositoryId, type);
        this.fileInfo = fileInfo;
        if (relatedResource != null) {
            addRelatedResource(relatedResource);
        }
        setPublishTime(publishTime);
        setFromDate(startTime);
        setToDate(endTime);
        setShortName(IOUtil.getFileTail(fileInfo.getUrl()));
        setLongName(fileInfo.getUrl());
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return this.fileInfo.toString();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public ResourceClass getResourceClass() {
        return CLASS_FILE;
    }


    /**
     *  Set the Fileinfo property.
     *
     *  @param value The new value for Fileinfo
     */
    public void setFileInfo(FileInfo value) {
        fileInfo = value;
    }

    /**
     *  Get the FileInfo property.
     *
     *  @return The FileInfo
     */
    public FileInfo getFileInfo() {
        return fileInfo;
    }


}
