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

import ucar.unidata.util.IOUtil;
import org.gsac.gsl.GsacArgs;
import org.gsac.gsl.GsacConstants;


import org.gsac.gsl.util.*;

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

    /** _more_ */
    private String siteID;

    /** _more_ */
    private Date publishTime;



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
        this.publishTime = publishTime;
        setFromDate(startTime);
        setToDate(endTime);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return this.fileInfo.toString();
    }

    public String getLabel() {
        return IOUtil.getFileTail(fileInfo.getUrl());
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


    /**
     *  Set the Site property.
     *
     *  @param value The new value for Site
     */
    public void xsetSiteID(String value) {
        siteID = value;
    }

    /**
     *  Get the Site property.
     *
     *  @return The Site
     */
    public String xgetSiteID() {
        return siteID;
    }

    /**
     *  Set the PublishTime property.
     *
     *  @param value The new value for PublishTime
     */
    public void setPublishTime(Date value) {
        publishTime = value;
    }

    /**
     *  Get the PublishTime property.
     *
     *  @return The PublishTime
     */
    public Date getPublishTime() {
        return publishTime;
    }


}
