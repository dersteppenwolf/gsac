/*
 * $Id: SopacFileManager.java 291 2011-11-02 21:46:04Z hankr $
 */

package edu.ucsd.sopac.projects.gsac.repository;

import org.gsac.gsl.*;
import org.gsac.gsl.model.*;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;

import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Calendar;
import java.util.Set;

/**
 * Handles all of the resource related repository requests
 *
 *
 */
public class SopacFileManager extends FileManager implements
		SopacProperties {

	private static final String IS_SITE_BASED_RESOURCE = "IS_SITE_BASED_RESOURCE";
	private static final String TRUE = "TRUE";
	private static final String FALSE = "FALSE";
	private static final String SITE_TYPE_MODE = "SITE_TYPE_MODE";
	
	/**
	 * ctor
	 *
	 * @param repository the repository
	 */
	public SopacFileManager(SopacRepository repository) {
		super(repository);
	}

	/**
	 * CHANGEME
	 * handle the request
	 *
	 * @param request The request
	 * @param response The response
	 *
	 * @throws Exception on badness
	 */
	public void handleRequest(GsacRequest request, GsacResponse response)
			throws Exception {
		
		System.err.println( "SopacFileManager.handleRequest" );

		long t1 = System.currentTimeMillis();
		StringBuffer msgBuff = new StringBuffer();
		List<Clause> clauses = new ArrayList<Clause>();
		List<String> tableNames = new ArrayList<String>();
		Set<String> tableNamesFromSiteClauses = new HashSet<String>();

		// add initial tables to table names.  we may remove SITE if a non-site 
		// type query
		tableNames.add(Tables.DATA_RECORD.NAME);
		tableNames.add(Tables.SITE.NAME);

		//////////////////////////////////////////////////////////////////////////
		// get and handle site clauses from SiteManager //////////////////////////
		//////////////////////////////////////////////////////////////////////////

		request.putProperty( "SITE_TYPE_MODE", "CONGPS" );
		request.putProperty( IS_SITE_BASED_RESOURCE, TRUE );
		List<Clause> siteClauses = getSiteManager().getSiteClauses(request,
				response, tableNames, msgBuff);

		//some file types do not require querying on SITE table
		tableNames = new ArrayList<String>();
		tableNames.add(Tables.DATA_RECORD.NAME);

		// set site table join clause, but don't add to clause list yet
		Clause monumentJoin = null;
		Clause affiliationJoin = null;
		Clause scgJoin = null;
		Clause coordinateSourceJoin = null;

		monumentJoin = Clause.join(Tables.SITE.COL_SITE_ID,
				Tables.DATA_RECORD.COL_SITE_ID);
		affiliationJoin = Clause.join(Tables.SITE.COL_SITE_ID,
				Tables.SITE_AFFILIATION.COL_SITE_ID);
		scgJoin = Clause.join(Tables.SITE.COL_SITE_ID,
				Tables.SITE_COORDINATES_GEODETIC.COL_SITE_ID);
		coordinateSourceJoin = Clause.join(
				Tables.SITE_COORDINATES_GEODETIC.COL_SOURCE_ID,
				Tables.COORDINATE_SOURCE.COL_SOURCE_ID);

		boolean addedMonumentJoin = false;
		boolean addedAffiliationJoin = false;
		boolean addedCoordinateJoins = false;
		boolean supportedFileType = false;
		String currentFileType = null;
		
		// add any site clauses
		if (siteClauses.size() > 0) {
			System.err.println("siteClauses list size > 0, add site clauses");

			tableNames.add(Tables.SITE.NAME);
			clauses.add(monumentJoin);
			addedMonumentJoin = true;

			// remove the SITE_TYPE_CODE clause we added in SopacSiteManager when we
			// perform the CONGPS and CAMGPS queries.  this will not remove the
			// site type selected by the user in the form (appears as "null" col/val below)
			Clause clauseToRemove = null;
			for (Clause c : siteClauses) {
				//String expr = (String) c.getExpr();
				//Object val = (Object) c.getValue();
				String col = (String) c.getColumn();
				//System.err.println("site clause list: clause col: " + col + " value: " + val
				//		+ " expr: " + expr);
				List<String> tnList = (List<String>) c
						.getTableNames(tableNames);
				for (String s : tnList) {
					//System.err.println("tnList string: " + s);					
					if (!tableNamesFromSiteClauses.contains(s))
						tableNamesFromSiteClauses.add(s);
				}

				if (col == null)
					continue;
				if (col.equals("SITE.SITE_TYPE_CODE")) {
					clauseToRemove = c;
					//System.err.println("remove this clause; col: " + col
					//		+ " val: " + val);

					// removed since we need to evaluate other clauses
					//break;
				}
			}
			if (!(clauseToRemove == null)) {
				//System.err.println("removing clause");
				siteClauses.remove(clauseToRemove);
				clauseToRemove = null;
			}

			//for (Clause c : siteClauses) {
			//	String val = (String) c.getValue();
			//	String col = (String) c.getColumn();
			//System.err.println("after removal: clause col: " + col
			//		+ " value: " + val);
			//}

			//System.err.println("site clauses size: " + siteClauses.size());
			if (siteClauses.size() > 0) {
				Clause siteClause = Clause.and(siteClauses);
				clauses.add(siteClause);
			}
		}

		// debugging
		//for (Clause c : clauses) {
		//	String val = (String) c.getValue();
		//	String col = (String) c.getColumn();
		//	System.err.println("master clauses list1 : clause col: " + col
		//			+ " value: " + val);
		//}

		// add any additional joins as a result of new clauses (e.g., 
		// SITE_AFFILIATION).  we don't add these joins in SopacSiteManager
		// since we use left joins

		for (String s : tableNamesFromSiteClauses) {
			//System.err.println("table name: " + s);

			if (s.equals(Tables.SITE_AFFILIATION.NAME)) {
				//System.err
				//		.println("Site clauses added site affiliation query.  Manually add join on this table.");

				clauses.add(affiliationJoin);
				addedAffiliationJoin = true;
			}

			if (s.equals(Tables.SITE_COORDINATES_GEODETIC.NAME)) {
				//System.err
				//		.println("Site clauses added site coordinates query.  " +
				//				"Manually add join on this table.  Manually add join on COORDINATE_SOURCE.  Add restriction " +
				//				"for mean WGS84 coords");

				clauses.add(scgJoin);
				clauses.add(coordinateSourceJoin);
				addedCoordinateJoins = true;
			}

		}

		//////////////////////////////////////////////////////////////////////////
		// BEGIN resource type restrictions //////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////

		// file size restrictions ///////////////////////////////////////////////

		if (request.defined(ARG_FILESIZE_MIN)) {
			clauses.add(Clause.ge(Tables.DATA_RECORD.COL_FILE_SIZE,
					request.get(ARG_FILESIZE_MIN, 0)));
			appendSearchCriteria(msgBuff, "Filesize&gt;=",
					"" + request.get(ARG_FILESIZE_MIN, 0));
		}

		if (request.defined(ARG_FILESIZE_MAX)) {
			clauses.add(Clause.le(Tables.DATA_RECORD.COL_FILE_SIZE,
					request.get(ARG_FILESIZE_MAX, 0)));
			appendSearchCriteria(msgBuff, "Filesize&lt;=",
					"" + request.get(ARG_FILESIZE_MAX, 0));
		}

		// file type restrictions ///////////////////////////////////////////////
		if (request.defined(ARG_FILE_TYPE)) {

			List<String> args = null;
			//add in the resource type clause
			//e.g.:
			/*
			 clauses.add(
			 Clause.or(
			 Clause.makeIntClauses(
			 "file type column",
			 args = (List<String>) request.getList(
			 ARG_FILE_TYPE))));
			 */

			//addSearchCriteria(msgBuff, "Resource Type", args,
			//                 ARG_FILE_TYPE);

			Clause dataTypeJoin = null;
			dataTypeJoin = Clause.join("DATA_RECORD.DATA_TYPE_ID",
					Tables.DATA_TYPE.COL_DATA_TYPE_ID);
			clauses.add(dataTypeJoin);

			List<Clause> fileTypeClauses = new ArrayList<Clause>();
			if (request.getList(ARG_FILE_TYPE).size() > 0) {
				
				args = (List<String>) request.getList(ARG_FILE_TYPE);
				for (String arg : args) {
					currentFileType = arg;
					//System.err.println("arg resource type: " + arg);
					// contain restrictions specific to a file type (and sub-type),
					// use "AND" here
					List<Clause> fileTypeSubTypeClauses = new ArrayList<Clause>();
					List<Clause> specificFileTypeClauses = new ArrayList<Clause>();

					// pj, 01/28/2011: vocab changes.  now "gnss.rinex.observation"
					if (arg.startsWith("gnss.data.rinex")
							|| arg.startsWith("gnss.product")) {

						// file type and sub type use AND
						// when we add a specific file/subfile type (e.g., rinex obs) to
						// another, we use OR

						if (arg.equals("gnss.data.rinex.observation")) {
							// Set the request property
							request.putProperty( IS_SITE_BASED_RESOURCE, TRUE );
							//System.err
							//		.println("arg equals 1, set dt to rinex, dst to obs");
							fileTypeSubTypeClauses.add(Clause.eq(
									Tables.DATA_TYPE.COL_DATA_TYPE, "rinex"));
							fileTypeSubTypeClauses.add(Clause.eq(
									Tables.DATA_TYPE.COL_DATA_SUB_TYPE, "obs"));
							supportedFileType = true;
						}
						if (arg.equals("gnss.data.rinex.navigation")) { // set DATA_TYPE to rinex
							request.putProperty( IS_SITE_BASED_RESOURCE, TRUE );
							fileTypeSubTypeClauses.add(Clause.eq(
									Tables.DATA_TYPE.COL_DATA_TYPE, "rinex"));
							fileTypeSubTypeClauses.add(Clause.eq(
									Tables.DATA_TYPE.COL_DATA_SUB_TYPE, "nav"));
							supportedFileType = true;
						}
						if (arg.equals("gnss.data.rinex.meteorology")) {
							request.putProperty( IS_SITE_BASED_RESOURCE, TRUE );
							fileTypeSubTypeClauses.add(Clause.eq(
									Tables.DATA_TYPE.COL_DATA_TYPE, "rinex"));
							fileTypeSubTypeClauses.add(Clause.eq(
									Tables.DATA_TYPE.COL_DATA_SUB_TYPE, "met"));
							supportedFileType = true;
						}

						if (arg.equals("gnss.product.sinex")) {
							request.putProperty( IS_SITE_BASED_RESOURCE, FALSE );
							fileTypeSubTypeClauses.add(Clause.eq(
									Tables.DATA_TYPE.COL_DATA_TYPE, "product"));
							fileTypeSubTypeClauses.add(Clause.eq(
									Tables.DATA_TYPE.COL_DATA_SUB_TYPE, "snx"));
							supportedFileType = true;
						}
						if (arg.equals("gnss.product.sp3")) {
							request.putProperty( IS_SITE_BASED_RESOURCE, FALSE );
							fileTypeSubTypeClauses.add(Clause.eq(
									Tables.DATA_TYPE.COL_DATA_TYPE, "product"));
							fileTypeSubTypeClauses.add(Clause.eq(
									Tables.DATA_TYPE.COL_DATA_SUB_TYPE, "sp3"));
							supportedFileType = true;
						}

					} else {
						// no sub type: raw, sitelog					
						//System.err.println("type has no sub type");

						if (arg.equals("gnss.metadata.igs_site_log.text")) {
							fileTypeSubTypeClauses.add(Clause.eq(
									Tables.DATA_TYPE.COL_DATA_TYPE, "sitelog"));
							fileTypeSubTypeClauses.add(Clause.eq(
									Tables.DATA_RECORD.COL_DIRECTORY, "/docs/site_logs"));
							fileTypeSubTypeClauses.add(Clause.like(
									Tables.DATA_RECORD.COL_FILE_BASENAME, "%.txt" ));
							supportedFileType = true;
						} else if (arg.equals("gnss.data.raw")) { //raw
							fileTypeSubTypeClauses.add(Clause.eq(
									Tables.DATA_TYPE.COL_DATA_TYPE, "raw"));
							supportedFileType = true;
						}
					}

					// within a specific file type, we use "AND" for type, sub type restriction
					specificFileTypeClauses.add(Clause
							.and(fileTypeSubTypeClauses));

					// then, each file type restriction group (type and sub type) is wrapped with
					// an "OR" restriction
					fileTypeClauses.add(Clause.or(specificFileTypeClauses));

				} // end ARG_FILE_TYPE loop

				//fileTypeClauses.add(Clause.and(specificFileTypeClauses));

				// top level restrictions use "OR"
				clauses.add(Clause.or(fileTypeClauses));

				List<Clause> miscResourceClauses = new ArrayList<Clause>();
				//			 add DATA_FILE_EXISTS restriction to all queries as an "AND"
				miscResourceClauses.add(Clause.eq(
						Tables.DATA_RECORD.COL_DATA_FILE_EXISTS_FLAG, 1));
				
				//System.err.println( "request.getProperty: " +  request.getProperty( IS_SITE_BASED_RESOURCE ) );

				// site-based file types only - add "SITE ID is not null"
				if (request.getProperty(IS_SITE_BASED_RESOURCE).equals(TRUE)) {	
					if (!addedMonumentJoin) {
						tableNames.add(Tables.SITE.NAME);
						clauses.add(monumentJoin);
						addedMonumentJoin = true;
					}
					miscResourceClauses.add(Clause
							.isNotNull(Tables.DATA_RECORD.COL_SITE_ID));
				}
				// remove any site-based clauses if a non-site based data type
				else {
					System.err.println("non site-based resource.  remove any site-based clauses");
					if (addedMonumentJoin) {
						tableNames.remove(Tables.SITE.NAME);
						clauses.remove(monumentJoin);
					}
					if (addedAffiliationJoin) {
						tableNames.remove(Tables.SITE_AFFILIATION.NAME);
						clauses.remove(affiliationJoin);
					}
					if (addedCoordinateJoins) {
						tableNames.remove(Tables.COORDINATE_SOURCE.NAME);
						clauses.remove(coordinateSourceJoin);
						tableNames
								.remove(Tables.SITE_COORDINATES_GEODETIC.NAME);
						clauses.remove(scgJoin);
					}
				}

				clauses.add(Clause.and(miscResourceClauses));
			} // end ARG_FILE_TYPE list is not null

		} else { // ARG_FILE_TYPE is null

			// handle cases where no data type given (e.g, user enters
			// http://geowk01.ucsd.edu:8080/gsacws/gsacapi/resource/search).
			// set resource type to null in these cases
			List<Clause> fileTypeClauses = new ArrayList<Clause>();
			fileTypeClauses.add(Clause.eq(Tables.DATA_TYPE.COL_DATA_TYPE,
					"null"));
			clauses.add(Clause.and(fileTypeClauses));
		}

		// publish date range ///////////////////////////////////////////////

		// these columns are not indexed and queries will be slower if data
		// dates are not part of query
		Date[] publishDateRange = request.getDateRange(
				ARG_FILE_PUBLISHDATE_FROM, ARG_FILE_PUBLISHDATE_TO,
				null, null);

		if (publishDateRange[0] != null) {
			java.sql.Date sqlBeginDate = new java.sql.Date(
					publishDateRange[0].getTime());
			clauses.add(Clause.ge(Tables.DATA_RECORD.COL_FILE_CREATE_TIME,
					sqlBeginDate));
			appendSearchCriteria(msgBuff, "Publish date&ge;=", ""
					+ format(publishDateRange[0]));
		}

		if (publishDateRange[1] != null) {
			// add 23:59:59 to end date (is this used in query?  not shown)
			Calendar cal = Calendar.getInstance();
			cal.setTime(publishDateRange[1]);
			cal.add(Calendar.HOUR, 23);
			cal.add(Calendar.MINUTE, 59);
			cal.add(Calendar.SECOND, 59);
			java.sql.Date sqlEndDate = new java.sql.Date(cal.getTimeInMillis());
			clauses.add(Clause.le(Tables.DATA_RECORD.COL_FILE_CREATE_TIME,
					sqlEndDate));
			appendSearchCriteria(msgBuff, "Publish date&le;=", ""
					+ format(publishDateRange[1]));
			cal = null;
		}

		// start/stop date range ///////////////////////////////////

		Date[] dataDateRange = request.getDateRange(ARG_FILE_DATADATE_FROM,
				ARG_FILE_DATADATE_TO, null, null);

		// TODO: now that we're only using year/doy virtual column, remove
		// appendSearchCriteria method calls here?
		if (dataDateRange[0] != null) {
			//clauses.add(Clause.ge(Tables.DATA_RECORD.COL_START_TIME,
			//		dataDateRange[0]));
			appendSearchCriteria(msgBuff, "Start date&ge;=", ""
					+ format(dataDateRange[0]));

			// need to include year in query of table, this column is indexed,
			// otherwise queries are very slow
			Calendar cal = Calendar.getInstance();
			cal.setTime(dataDateRange[0]);
			int year = cal.get(Calendar.YEAR);
			int doy = cal.get(Calendar.DAY_OF_YEAR);
			int yearDoy = year * 1000 + doy;
			clauses.add(Clause.ge("DATA_RECORD.YEARDOY", // add this col to Tables.java
					yearDoy));
			cal = null;
		}

		if (dataDateRange[1] != null) {
			//clauses.add(Clause.le(Tables.DATA_RECORD.COL_STOP_TIME,
			//		dataDateRange[1]));
			appendSearchCriteria(msgBuff, "End date&le;=", ""
					+ format(dataDateRange[1]));

			Calendar cal = Calendar.getInstance();
			cal.setTime(dataDateRange[1]);
			int year = cal.get(Calendar.YEAR);
			int doy = cal.get(Calendar.DAY_OF_YEAR);
			int yearDoy = year * 1000 + doy;
			clauses.add(Clause.le("DATA_RECORD.YEARDOY", // add this col to Tables.java
					yearDoy));
		}

		// perform resource query //////////////////////////////////////////
		// debugging
		//for (Clause c : clauses) {
		//	String col = (String) c.getColumn();
		//  System.err.println("master clauses list2 : clause col: " + col);
		//}
		Clause mainClause = Clause.and(clauses);
		//System.err.println("select stmt");

		// return if the resource type given is not supported.  this can't happen via the web form, but
		// can from a wget, where you can provide any resource type.  also will occur if user
		// selects "any" from list of file types, not supported.
		if (!supportedFileType) {
			response.appendMessage("ERROR: The SOPAC GSAC service does not currently support the given resource type (" + 
					currentFileType + "), or the \"any\" option.");
			return;
		}
		
		Boolean isSiteBasedResource = request.getProperty(IS_SITE_BASED_RESOURCE).equals(TRUE);
		
		Statement statement = getDatabaseManager().select(
				getResourceColumns(isSiteBasedResource),
				mainClause.getTableNames(tableNames), mainClause);
		int cnt=0;
		try {
			SqlUtil.Iterator iter = SqlUtil.getIterator(statement,
					request.getOffset(), request.getLimit());
			while (iter.getNext() != null) {
				response.addResource(makeResource(iter.getResults(),isSiteBasedResource));
				cnt++;
				if (!iter.countOK()) {
					response.setExceededLimit();
					break;
				}
			}
			iter.close();
			//System.err.println("closing connection");
		} finally {
			getDatabaseManager().closeAndReleaseConnection(statement);
		}
		long t2 = System.currentTimeMillis();
		System.err.println("read " + cnt + " resources in " + (t2 - t1) + "ms");

		response.appendMessage("Note: The SOPAC GSAC service does not currently support access to campaign data.<br>");

		setSearchCriteriaMessage(response, msgBuff);
	}

	/**
	 * CHANGEME
	 * Get the columns to select for resources
	 * @return resource columns
	 */
	private String getResourceColumns(Boolean isSiteBasedResource) {
		//return "files.column1,files.column2, etc";

		// path, fileSize, md5),
		// site, publishTime, fromTime, toTime,
		// toFileType(type));

		if (isSiteBasedResource) {
			return ("DATA_RECORD.SITE_ID,DATA_RECORD.FILE_BASENAME,DATA_RECORD.YEAR,DATA_RECORD.DAY,"
					+ "DATA_RECORD.CHECK_SUM,DATA_RECORD.START_TIME,DATA_RECORD.STOP_TIME,DATA_RECORD.FILE_CREATE_TIME,"
					+ "DATA_RECORD.FILE_LOCATION,DATA_RECORD.PERCENT_COMPLETE,DATA_RECORD.INSERT_DATE,"
					+ "DATA_RECORD.FILE_SIZE,DATA_RECORD.DATA_RECORD_ID,DATA_TYPE.DATA_TYPE,DATA_TYPE.DATA_SUB_TYPE");
		} else {
			return ("DATA_RECORD.FILE_BASENAME,DATA_RECORD.YEAR,DATA_RECORD.DAY,"
					+ "DATA_RECORD.CHECK_SUM,DATA_RECORD.START_TIME,DATA_RECORD.STOP_TIME,DATA_RECORD.FILE_CREATE_TIME,"
					+ "DATA_RECORD.FILE_LOCATION,DATA_RECORD.PERCENT_COMPLETE,DATA_RECORD.INSERT_DATE,"
					+ "DATA_RECORD.FILE_SIZE,DATA_RECORD.DATA_RECORD_ID,DATA_TYPE.DATA_TYPE,DATA_TYPE.DATA_SUB_TYPE");

		}
	}

	
	/**
	 * 
	 * Create a resource from the given results
	 *
	 * @param results result set
	 *
	 * @return The resource
	 *
	 * @throws Exception On badness
	 */
	public GsacFile makeResource(ResultSet results, Boolean isSiteBasedResource) throws Exception {
		
		//System.err.println("makeResource...");

		int col = 1;
		/*
		 String exportID = results.getString(col++);
		 String fileID = results.getString(col++);
		 int archiveTypeID = results.getInt(col++);
		 int exportTypeID = results.getInt(col++);
		 int siteID = results.getInt(col++);
		 long fileSize = results.getLong(col++);
		 String path = results.getString(col++);
		 Date publishTime = results.getDate(col++);
		 String md5 = results.getString(col++);
		 int sampleInterval = results.getInt(col++);
		 */

		// we may not be returning a site ID
		String siteID = null;
		if (isSiteBasedResource) {
			siteID = results.getString(col++);
			//System.err.println("siteID: " + siteID );
		}
		
		String basename = results.getString(col++);
		int year = results.getInt(col++);
		int doy = results.getInt(col++);
		String md5 = results.getString(col++).replace("\\s+$",""); 
		Date fromTime = results.getDate(col++); // start_date
		Date toTime = results.getDate(col++); // end_date
		Date publishTime = results.getDate(col++);

		String path = getPath(results.getString(col++)); // file_location
		
		int pctComplete = results.getInt(col++);
		Date insertDate = results.getDate(col++);
		long fileSize = results.getLong(col++);
		int dataRecordId = results.getInt(col++);
		String dataType = results.getString(col++);
		String dataSubType = results.getString(col++);

		String exportID = "sopac." + dataRecordId; // repository id in GsacFile

		// TODO: CHANGE to id based on file type (Paul's note ???)
		ExportType type = ExportType.findType(ExportType.GROUP_ALL_TYPES,
				getExportTypeId(dataType, dataSubType)); 

		//System.err.println("makeResource(): site: " + siteID + " path: " + _path + " publish date: " + publishTime);

		GsacSite site = null;

		if (!(siteID == null)) {
			site = getSiteManager().getSiteForResource(siteID);
		}

		GsacFile resource = new GsacFile(exportID, new FileInfo(path,
				fileSize, md5), site, publishTime, fromTime, toTime,
				toResourceType(type));
		return resource;
	}

	
	private int getExportTypeId(String dataType, String dataSubType) {
		//System.err.println("setExportTypeId: dataType: " + dataType + " dataSubType: " + dataSubType);

		if (dataType.equals("rinex") && dataSubType.equals("obs"))
			return 1;
		if (dataType.equals("rinex") && dataSubType.equals("nav"))
			return 2;
		if (dataType.equals("rinex") && dataSubType.equals("met"))
			return 3;
		if (dataType.equals("raw"))
			return 4;
		if (dataType.equals("sitelog"))
			return 5;
		if (dataType.equals("products") && dataSubType.equals("sinex"))
			return 6;
		if (dataType.equals("products") && dataSubType.equals("sp3"))
			return 7;

		return 0;
	}


	
	private String getPath(String path) {
		//System.err.println("in getpath()");
		// change any lox hostnames to garner
		if (path != null) {

			//System.err.println("in setpath(), not null");
			if (path.contains("lox.ucsd.edu")) {

				//System.err.println("in setpath(), replace string");
				path = path.replaceFirst("lox.ucsd.edu",
						SopacProperties.SOPAC_ARCHIVE_HOSTNAME);
			}
		}
		return path;
	}

	
	/**
	 *
	 * @param resourceId _more_
	 *
	 * @return _more_
	 *
	 * @throws Exception _more_
	 */
	public GsacResource getResource(String resourceId) throws Exception {
		
		//System.err.println("GsacResource, resourceId: " + resourceId );
		
		if ((resourceId == null) || (resourceId.length() == 0)) {
			return null;
		}

		StringBuffer msgBuff = new StringBuffer();
		List<Clause> clauses = new ArrayList<Clause>();
		List<String> tableNames = new ArrayList<String>();
		Set<String> tableNamesFromSiteClauses = new HashSet<String>();

		// add initial tables to table names.  we may remove SITE if a non-site 
		// type query
		tableNames.add(Tables.DATA_RECORD.NAME);
//		List<Clause> fileClauses = new ArrayList<Clause>();
		Clause clause = Clause.eq( Tables.DATA_RECORD.COL_DATA_RECORD_ID, resourceId.replace("sopac.", ""));

		Statement statement = getDatabaseManager().select(
                   "DATA_RECORD.FILE_BASENAME, DATA_RECORD.YEAR, DATA_RECORD.DAY,"
                 + "DATA_RECORD.CHECK_SUM, DATA_RECORD.START_TIME, DATA_RECORD.STOP_TIME, DATA_RECORD.FILE_CREATE_TIME,"
                 + "DATA_RECORD.FILE_LOCATION, DATA_RECORD.PERCENT_COMPLETE, DATA_RECORD.INSERT_DATE,"
                 + "DATA_RECORD.FILE_SIZE, DATA_RECORD.DATA_RECORD_ID, DATA_TYPE_ID",
			clause.getTableNames(tableNames), clause);
		int cnt=0;
		try {
            ResultSet results = statement.getResultSet();
            while (results.next()) {
            	// TODO: does API allow multiple files?


            	String baseName = results.getString(1);
            	//System.err.println("baseName: " + baseName);
            	String checkSum = results.getString(4);
        		Date fromTime = results.getDate( 5 ); // start_date
        		Date toTime = results.getDate( 6 ); // end_date
            	String location = results.getString(8);
            	//System.err.println("location: " + location );
        		Date publishTime = results.getDate( 10 );
        		long fileSize = results.getLong( 11 );
        		//System.err.println( "FileSize: " + fileSize );
        		int dataTypeID = results.getInt( 13 );
        		//System.err.println( "dataTypeID: " + dataTypeID );
		
            	// TODO: here CHANGE to id based on file type (Paul's note ???)
        		int typeId = 0;

        		// TODO: get this from the database, but 
        		switch( dataTypeID ) {
        		case 21: typeId = getExportTypeId( "products", "sinex"); break;
        		case 66: typeId = getExportTypeId( "products", "sp3" ); break;
        		case 72: typeId = getExportTypeId( "raw", "" ); break;
        		case 73: typeId = getExportTypeId( "raw", "" ); break;
        		case 74: typeId = getExportTypeId( "raw", "" ); break;
        		case 75: typeId = getExportTypeId( "raw", "" ); break;
        		case 76: typeId = getExportTypeId( "rinex", "met" ); break;
        		case 77: typeId = getExportTypeId( "rinex", "nav" ); break;
        		case 78: typeId = getExportTypeId( "rinex", "obs" ); break;
        		case 81: typeId = getExportTypeId( "sitelog", "" ); break;
        		default: typeId = 0;
        		}
        		
        		ExportType type = ExportType.findType(ExportType.GROUP_ALL_TYPES, typeId); 

            	GsacFile resource = new GsacFile( resourceId,
            			new FileInfo( location, fileSize, checkSum),
            				null,
            				publishTime,
            				fromTime,
            				toTime,
            				toResourceType(type) );

            	return resource;
            }
            
//            GsacFile resource = new GsacFile(exportID, new FileInfo(_path,
//                    fileSize, md5), site, publishTime, fromTime, toTime,
//                    toResourceType(type));
		
		} finally {
			getDatabaseManager().closeAndReleaseConnection(statement);
		}
		//long t2 = System.currentTimeMillis();
		//System.err.println("read " + cnt + " resources in " + (t2 - t1) + "ms");
		
		return null;
	}


	/**
	 * helper method
	 *
	 * @return sitemanager
	 */
	public SopacSiteManager getSiteManager() {
            return (SopacSiteManager) getRepository().getResourceManager(GsacSite.CLASS_SITE);
	}

	/**
	 * utility to convert an ExportType to the GSL ResourceType
	 *
	 * @param type Our type
	 *
	 * @return the gsl type
	 */
	public ResourceType toResourceType(ExportType type) {
		if (type == null) {
			return new ResourceType("");
		}
		return new ResourceType(type.getId() + "", type.getName());
	}

	/**
	 * utility to convert an array of  ExportTypes (from org.unavco.database.oracle) to the GSL ResourceType
	 *
	 * @param types Our types
	 *
	 * @return the gsl types
	 */
	public List<ResourceType> toResourceTypes(ExportType[] types) {
		List<ResourceType> resourceTypes = new ArrayList<ResourceType>();
		for (ExportType type : types) {
			addLabel(ARG_FILE_TYPE, "" + type.getId(), type.getName());
			System.err.println("type id: " + type.getId() + " name: "
					+ type.getName());
			resourceTypes.add(toResourceType(type));
		}
		return resourceTypes;
	}

	public List<Capability> doGetQueryCapabilities() {
		try {
			List<Capability> capabilities = new ArrayList<Capability>();
			// pj, 02/16/2011: this is not required to replace doGetResourceTypes,
			// addDefault* does it by default
			//capabilities.add(new Capability(ARG_FILE_TYPE, "Resource Type", makeVocabulary(ARG_FILE_TYPE), true));						
			addDefaultCapabilities(capabilities);
                        capabilities.addAll(getSiteManager().doGetQueryCapabilities());
			return capabilities;
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}

}
