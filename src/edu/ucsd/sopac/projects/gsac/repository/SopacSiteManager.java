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
 * $Id: SopacSiteManager.java 312 2011-11-30 17:22:03Z hankr $
 */

package edu.ucsd.sopac.projects.gsac.repository;

import org.gsac.gsl.*;
import org.gsac.gsl.model.*;
import org.gsac.gsl.metadata.*;
import org.gsac.gsl.metadata.gnss.*;
import org.gsac.gsl.util.*;
import org.apache.log4j.Logger;

import org.ramadda.sql.Clause;
import org.ramadda.sql.SqlUtil;
import edu.ucsd.sopac.projects.gsac.repository.utils.*;

import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * Example site manager.
 *
 * @author         Jeff McWhirter
 */
public class SopacSiteManager extends SiteManager {

	// CONGPS
	// assumptions: single SITE_AFFILIATION.ARRAY_CODE, SITE_DATA.dates per site;
	// when looping over rows, use a monId set of unique ids to handle multiple
	// llh per site.

	private static final String CONGPS_SITE_WHAT = SqlUtil.comma(new String[] {
			"SITE.SITE_ID", 
			"SITE.SITE_CODE", 
			"SITE.SITE_NAME",
			"SITE_COORDINATES_GEODETIC.LAT", 
			"SITE_COORDINATES_GEODETIC.LON",
			"SITE_COORDINATES_GEODETIC.ELLIP_HT", 
			"SITE_DATA.FIRST_YEAR",
			"SITE_DATA.FIRST_DAY", 
			"SITE_DATA.LAST_YEAR", 
			"SITE_DATA.LAST_DAY",
			"SITE_AFFILIATION.ARRAY_CODE" });

	// CAMGPS
	// get all campaigns a site is in.  get the min/max date for each campaign
	// (these will get min/maxed in SopacCamgpsSite class below).  multiple llh
	// values will get written over in SCS class.  use left joins to get
	// CMP and GC table info - may not be present in these tables.
	// sample query:
	/*
	 * SELECT
	 s.site_id,
	 s.site_code,
	 s.site_name,
	 scg.lat,
	 scg.lon,
	 scg.ellip_ht,
	 min (cmp.start_date),
	 max(cmp.end_date) ,
	 gc.name
	 FROM
	 site s
	 INNER JOIN
	 site_coordinates_geodetic scg
	 ON
	 s.site_id = scg.site_id
	 INNER JOIN
	 campaign_monument cm
	 ON
	 s.site_id = cm.monument_id
	 LEFT JOIN
	 geodetic_campaign gc
	 ON
	 gc.id = cm.geodetic_campaign_id
	 LEFT JOIN
	 campaign_monument_profile cmp
	 ON
	 cm.id = cmp.campaign_monument_id
	 WHERE
	 s.site_id = '01ae02'
	 GROUP BY
	 s.site_id,
	 s.site_code,
	 s.site_name,
	 scg.lat,
	 scg.lon,
	 scg.ellip_ht,
	 gc.name
	 */

	private static final String CAMGPS_SITE_WHAT = SqlUtil.comma(new String[] {
			"SITE.SITE_ID",
			"SITE.SITE_CODE",
			"SITE.SITE_NAME",
			"SITE_COORDINATES_GEODETIC.LAT",
			"SITE_COORDINATES_GEODETIC.LON",
			"SITE_COORDINATES_GEODETIC.ELLIP_HT",
			//			Tables.CAMPAIGN_MONUMENT_PROFILE.COL_START_DATE,
			//			Tables.CAMPAIGN_MONUMENT_PROFILE.COL_END_DATE,
			//			"min (" + Tables.CAMPAIGN_MONUMENT_PROFILE.COL_START_DATE + ")",
			//			"max (" + Tables.CAMPAIGN_MONUMENT_PROFILE.COL_END_DATE + ")",

			"min (PGM.CAMPAIGN_MONUMENT_PROFILE.START_DATE)",
			"max (PGM.CAMPAIGN_MONUMENT_PROFILE.END_DATE)",
			"GEODETIC_CAMPAIGN.NAME" });

	private static final String CAMGPS_SITE_GROUP_BY = " GROUP BY "
			+ SqlUtil.comma(new String[] { "SITE.SITE_ID", "SITE.SITE_CODE",
					"SITE.SITE_NAME", "SITE_COORDINATES_GEODETIC.LAT",
					"SITE_COORDINATES_GEODETIC.LON",
					"SITE_COORDINATES_GEODETIC.ELLIP_HT",
					"GEODETIC_CAMPAIGN.NAME" });

	/** CHANGEME Default query order. Set this to what you want to sort on */
	private static final String SITE_ORDER = " ORDER BY  " + "SITE.SITE_CODE" + " ASC ";

	/**
	 *   Extra columns we can search on.
	 *   Of the form: url argument, db column name, label
	 */
	private static String[][] _searchColumns = {
			//        { GsacExtArgs.ARG_REGION, Tables.MV_DAI_PRO.COL_REGION_1_NAME,
			//          "Region" },

			{ GsacExtArgs.ARG_CITY, Tables.SITE.COL_CITY, "City" },
			{ GsacExtArgs.ARG_COUNTRY, Tables.SITE.COL_COUNTRY, "Country" },
			{ GsacExtArgs.ARG_STATE, Tables.SITE.COL_STATE, "State" } };

	public static final String ARG_MONUMENT_SEARCHTYPE = GsacExtArgs.ARG_MONUMENT
			+ ".searchtype";

	/** Caches sites */
	private TTLCache<Object, GsacSite> exportSiteCache = new TTLCache<Object, GsacSite>(
			TTLCache.MS_IN_A_DAY);

	private static final int NUM_DAYS_FOR_SITE_INACTIVE = 365;

	private static final String SITE_TYPE_MODE = "SITE_TYPE_MODE";
	private static final String CONGPS = "CONGPS";
	private static final String CAMGPS = "CAMGPS";
	
	//static Logger logger = Logger.getLogger(GsacRepository.class);


	////////////////////////// END DECLARATIONS ///////////////////////////////////////////////////

	/**
	 * ctor
	 *
	 * @param repository the repository
	 */
	public SopacSiteManager(SopacRepository repository) {
		super(repository);

	     SqlUtil.debug = true; // comment out to turn off sql stmt output

	}

	/**
	 * 
	 * handlerequest
	 * 
	 * @param request The GSAC request
	 * @param response The GSAC response
	 */
	public void handleRequest(GsacRequest request, GsacResponse response)
			throws Exception {
		System.err.println("###### SopacSiteManager.handleSiteRequest()...");
		//logger.info("handleSiteRequest hit");
		//long t1 = System.currentTimeMillis();
		
		// Campaign and Continuous site searches require very different SQL
		// statements.  If no type is specified, then both queries must be 
		// run.
		
		List<String> siteTypes = request.getList(GsacArgs.ARG_SITE_TYPE);

		if ( siteTypes.isEmpty() ) {
			conGPSQuery( request, response );
			camGPSQuery( request, response );
		} else {
			for ( String siteType : siteTypes ) {
				//System.err.println( "siteType: " + siteType );
				if ( siteType.equals(CONGPS) ) conGPSQuery( request, response );
				if ( siteType.equals(CAMGPS) ) camGPSQuery( request, response );
			}
		}
		
	}
	
	
	/**
	 * conGPSQuery Continuous GPS Site Query
	 * 
	 * @param request The GSAC request
	 * @param response The GSAC response
	 * @throws Exception
	 * 
	 */
	public void conGPSQuery(GsacRequest request, GsacResponse response)
			throws Exception {

		List<String> tableNames = new ArrayList<String>();
		Clause clause;
		String sqlBetweenFromAndWhere;
		Statement statement;

		request.putProperty( SITE_TYPE_MODE, CONGPS );
		//System.err.println( "property: " + request.getProperty(SITE_TYPE_MODE) );
		
		tableNames.add(Tables.SITE.NAME); // manually set table names (we're using LEFT JOIN syntax)
		clause = getSiteClause(request, response, tableNames);

		StringBuffer sb = new StringBuffer();
		sb.append("LEFT JOIN SITE_DATA SITE_DATA ON "
				+ "SITE.SITE_ID = SITE_DATA.SITE_ID "
				+ "INNER JOIN SITE_AFFILIATION SITE_AFFILIATION ON "
				+ "SITE.SITE_ID = SITE_AFFILIATION.SITE_ID "
				+ "INNER JOIN SITE_COORDINATES_GEODETIC SITE_COORDINATES_GEODETIC  ON "
				+ "SITE.SITE_ID = SITE_COORDINATES_GEODETIC.SITE_ID  "
				// pj, 12/08/2010: use mean coords source restriction below to get single llh/site	
				+ "INNER JOIN COORDINATE_SOURCE COORDINATE_SOURCE  ON "
				+ "COORDINATE_SOURCE.SOURCE_ID = SITE_COORDINATES_GEODETIC.SOURCE_ID  ");

		// tectonic plate
		if (request.defined(GsacExtArgs.ARG_TECTONICPLATE)) {
			sb.append("LEFT JOIN tectonic_plate tectonic_plate ON "
					+ "tectonic_plate.tectonic_plate_id = site.tectonic_plate_id ");
		}

		// antenna, receiver, dome (SITE_TRANSACTION fields)
		//System.err.println("GsacExtArgs.ARG_ANTENNA: " + request.defined(GsacExtArgs.ARG_ANTENNA));
		if (request.defined(GsacExtArgs.ARG_ANTENNA)
				|| request.defined(GsacExtArgs.ARG_DOME)
				|| request.defined(GsacExtArgs.ARG_RECEIVER)
				|| request.defined(GsacExtArgs.ARG_MONUMENT)
				|| request.defined(GsacExtArgs.ARG_HAS_METPACK)) {
			sb.append("LEFT JOIN SITE_TRANSACTION_LOG SITE_TRANSACTION_LOG ON "
					+ "SITE_TRANSACTION_LOG.SITE_ID = SITE.SITE_ID ");
		}

		// perform query
		sqlBetweenFromAndWhere = sb.toString();
		//System.err.println("select stmt 1");
		statement = getDatabaseManager().select(getCongpsSiteSelectColumns(),
				tableNames, clause, sqlBetweenFromAndWhere,
				getSiteSelectSuffix(request), -1);
		processStatement(request, response, statement, request.getOffset(),
				request.getLimit());

	}

	
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 * 
	 */
	public void camGPSQuery(GsacRequest request, GsacResponse response)
			throws Exception {

		List<String> tableNames = new ArrayList<String>();
		Clause clause;
		String sqlBetweenFromAndWhere;
		Statement statement;

		request.putProperty( SITE_TYPE_MODE, CAMGPS );
		clause = null;
		tableNames = new ArrayList<String>();
		tableNames.add(Tables.SITE.NAME);
		clause = getSiteClause(request, response, tableNames);

		StringBuffer sb = new StringBuffer();
		sb.append("INNER JOIN  site_coordinates_geodetic site_coordinates_geodetic ON "
				+ "site.site_id = site_coordinates_geodetic.site_id "
				+ "INNER JOIN campaign_monument campaign_monument ON "
				+ "site.site_id = campaign_monument.monument_id "
				+ "LEFT JOIN geodetic_campaign geodetic_campaign ON "
				+ "geodetic_campaign.id = campaign_monument.geodetic_campaign_id "
				+ "LEFT JOIN PGM.campaign_monument_profile campaign_monument_profile ON "
				+ "campaign_monument.id = PGM.campaign_monument_profile.campaign_monument_id "
				// pj, 12/08/2010: use mean coords source restriction below to get single llh/site	
				+ "INNER JOIN COORDINATE_SOURCE COORDINATE_SOURCE  ON "
				+ "COORDINATE_SOURCE.SOURCE_ID = SITE_COORDINATES_GEODETIC.SOURCE_ID  ");

		// tectonic plate
		//System.err.println("GsacExtArgs.ARG_TECTONICPLATE: " + request.defined(GsacExtArgs.ARG_TECTONICPLATE));
		if (request.defined(GsacExtArgs.ARG_TECTONICPLATE)) {
			sb.append("LEFT JOIN tectonic_plate tectonic_plate ON "
					+ "tectonic_plate.tectonic_plate_id = site.tectonic_plate_id ");
		}

		// antenna, receiver, dome (PGM.EQUIPMENT tables)
		//System.err.println("GsacExtArgs.ARG_ANTENNA: " + request.defined(GsacExtArgs.ARG_ANTENNA));
		if (request.defined(GsacExtArgs.ARG_ANTENNA)
				|| request.defined(GsacExtArgs.ARG_DOME)
				|| request.defined(GsacExtArgs.ARG_RECEIVER)) { 
			// join the MONUMENT_EQUIPMENT_PROFILE, EQUIPMENT_UNIT, MANUFAGURED_EQUIPMENT, EQUIPMENT_TYPE tables
			sb.append(" LEFT JOIN PGM.MONUMENT_EQUIP_PROFILE MONUMENT_EQUIP_PROFILE  ON "
					+ "PGM.MONUMENT_EQUIP_PROFILE.MONUMENT_PROFILE_ID = PGM.CAMPAIGN_MONUMENT_PROFILE.ID "
					+ "LEFT JOIN PGM.EQUIPMENT_UNIT EQUIPMENT_UNIT ON "
					+ "PGM.EQUIPMENT_UNIT.ID = PGM.MONUMENT_EQUIP_PROFILE.EQUIPMENT_UNIT_ID "
					+ "LEFT JOIN PGM.MANUFACTURED_EQUIPMENT MANUFACTURED_EQUIPMENT ON "
					+ "PGM.MANUFACTURED_EQUIPMENT.ID = PGM.EQUIPMENT_UNIT.MFG_EQUIPMENT_ID "
					+ "LEFT JOIN PGM.EQUIPMENT_TYPE EQUIPMENT_TYPE ON "
					+ "PGM.EQUIPMENT_TYPE.ID = PGM.MANUFACTURED_EQUIPMENT.TYPE_ID ");
		} else if (request.defined(GsacExtArgs.ARG_MONUMENT)) { // join the MONUMENT_DESCRIPTION, MONUMENT_CLASS tables
			sb.append(" LEFT JOIN PGM.MONUMENT_DESCRIPTION MONUMENT_DESCRIPTION  ON "
					+ "PGM.MONUMENT_DESCRIPTION.MONUMENT_ID = CAMPAIGN_MONUMENT.MONUMENT_ID "
					+ "LEFT JOIN PGM.MONUMENT_CLASS MONUMENT_CLASS ON "
					+ "PGM.MONUMENT_CLASS.ID = PGM.MONUMENT_DESCRIPTION.MONUMENT_CLASS_ID ");
		}

		sqlBetweenFromAndWhere = sb.toString();
		//System.err.println("select stmt 2");
		statement = getDatabaseManager().select(getCamgpsSiteSelectColumns(),
				tableNames, clause, sqlBetweenFromAndWhere,
				getSiteSelectSuffix(request), -1);

		processStatement(request, response, statement, request.getOffset(),
				request.getLimit());
		
	}

	
	/**
	 *
	 * Get the list of site query clauses.
	 * This is called during a search for station(s).
	 *
	 * SOPAC repository-supported searches:
	 * codes, groups, city, country, state, antenna, receiver, dome, monument type, tectonic plage
	 *
	 * @param request the resquest
	 * @param response the response
	 * @param msgBuff buffer to append search criteria to
	 *
	 * @return list of clauses for selecting sites
	 */
	public List<Clause> getSiteClauses(GsacRequest request,
			GsacResponse response, List<String> tableNames, StringBuffer msgBuff) {

		//System.err.println("SopacSiteManager.getSiteClauses: ");

		// Get the request property
		String siteTypeProp = (String) request.getProperty( SITE_TYPE_MODE );
		//System.err.println( "site_type_mode: " + siteTypeProp );
		Mode.SiteTableSiteType siteTypeMode = null;
		if ( siteTypeProp.equals(CONGPS) ) {
			siteTypeMode = Mode.SiteTableSiteType.CONGPS;
		} else if ( siteTypeProp.equals(CAMGPS) ) {
			siteTypeMode = Mode.SiteTableSiteType.CAMGPS;
		}
	
		// table join clauses are now handled as LEFT JOINS, and are set in sqlBetweenFromAndWhere
		// in handleSiteRequest() above, instead of here.

		List<Clause> clauses = new ArrayList<Clause>();

		// restrict all queries to use WGS84 mean llh record (to get one llh/site)
		clauses.add(Clause.eq(Tables.COORDINATE_SOURCE.COL_SOURCE_NAME,	"MEAN WGS84"));

		// site type restriction ///////////////////////////////////////////
		
		//System.err.println("getSiteClauses. site_type_mode: " + request.getProperty(SITE_TYPE_MODE));
		if ( siteTypeMode.equals( Mode.SiteTableSiteType.CONGPS ) ) {
			clauses.add(Clause.eq(Tables.SITE.COL_SITE_TYPE_CODE, "CONGPS"));
		} else if ( siteTypeMode.equals( Mode.SiteTableSiteType.CAMGPS ) ) {
			clauses.add(Clause.eq(Tables.SITE.COL_SITE_TYPE_CODE, "CAMGPS"));
		}

		// form request restrictions ////////////////////////////////////////////

		// lat/lon
		if (request.defined(ARG_NORTH)) {
			//                        clauses.add(Clause.le(Tables.MV_DAI_PRO.COL_MON_LAT_NUM,
			clauses.add(Clause.le(Tables.SITE_COORDINATES_GEODETIC.COL_LAT,
					request.get(ARG_NORTH, 0.0)));
			appendSearchCriteria(msgBuff, "north&lt;=",
					"" + request.get(ARG_NORTH, 0.0));
		}
		if (request.defined(ARG_SOUTH)) {
			clauses.add(Clause.ge(Tables.SITE_COORDINATES_GEODETIC.COL_LAT,
					request.get(ARG_SOUTH, 0.0)));
			appendSearchCriteria(msgBuff, "south&gt;=",
					"" + request.get(ARG_SOUTH, 0.0));
		}
		if (request.defined(ARG_EAST)) {
			clauses.add(Clause.le(Tables.SITE_COORDINATES_GEODETIC.COL_LON,
					request.get(ARG_EAST, 0.0)));
			appendSearchCriteria(msgBuff, "east&lt;=",
					"" + request.get(ARG_EAST, 0.0));
		}
		if (request.defined(ARG_WEST)) {
			clauses.add(Clause.ge(Tables.SITE_COORDINATES_GEODETIC.COL_LON,
					request.get(ARG_WEST, 0.0)));
			appendSearchCriteria(msgBuff, "west&gt;=",
					"" + request.get(ARG_WEST, 0.0));
		}

		List<String> args = null;
		if (request.defined(ARG_SITE_ID)) {
			//Here we use makeIntClauses for the site id
			// pj: it's a string at SOPAC
			clauses.add(Clause.or(Clause.makeStringClauses(
					//             Tables.MV_DAI_PRO.COL_MON_ID,
					Tables.SITE.COL_SITE_ID,
					args = (List<String>) request.getList(ARG_SITE_ID))));

			addSearchCriteria(msgBuff, "Site ID", args);
		}

		// need to handle SITE STATUS differently than above - we will see if last day of 
		// data is/isn't greater than current day - 365 days
		if ( siteTypeMode.equals( Mode.SiteTableSiteType.CONGPS  )) { //CONGPS only

			String[][] statusArgs = { { GsacArgs.ARG_SITE_STATUS,
					Tables.SITE_DATA.COL_LAST_YEARDOY, "Site Status" } };

			/* two problems here: SITE_DATA.LAST_YEARDOY not found (but is found in dbvis); 
			 * virtual column returned as null
			 * when using multiple joins (in dbvis).  for now, how do we enable status in results but not
			 * as a search option?
			 */
			for (String[] argValues : statusArgs) {
				if (request.defined(argValues[0])) {

					// calculate the minimum active year/doy (30 days prior to today)
					List<Clause> statusClauses = new ArrayList<Clause>();
					DateTime dtNow = new DateTime();
					DateTime dtInactive = dtNow.minusDays(NUM_DAYS_FOR_SITE_INACTIVE);					
					int year = dtInactive.getYear();
					int dayOfYear = dtInactive.getDayOfYear();					
					int minimumActiveYearDoy = year * 1000 + dayOfYear;
					System.err.println("min active year doy: "
							+ minimumActiveYearDoy);
					dtNow=null;
					dtInactive=null;

					//There might be more than one argument and also it can be comma separated
					args = (List<String>) request
							.getDelimiterSeparatedList(argValues[0]);
					//System.err.println("arg values[1]: " +  argValues[1]);

					for (Object arg : args) {
						//System.err.println("arg: " + arg.toString());
						if (arg.toString().equals("active")) {
							//System.err.println("add active clause");
							statusClauses.add(Clause.ge(argValues[1],
							//						args)));
									minimumActiveYearDoy));

						} else if (arg.toString().equals("inactive")) {
							//System.err.println("add inactive clause");
							statusClauses.add(Clause.le(argValues[1],
							//								args)));
									minimumActiveYearDoy - 1));

						}						
					}
					//System.err.println("add status clauses to master clause");
					clauses.add(Clause.or(statusClauses));
					
					addSearchCriteria(msgBuff, argValues[2], args);

					//				clauses.add(Clause.or(Clause.makeStringClauses(argValues[1],
					//						args)));
					//				addSearchCriteria(msgBuff, argValues[2], args);
				}
			}
		}

		// SITE CODE, SITE NAME restrictions ///////////////////////////////////////////////////////
		addStringSearch(request, ARG_SITECODE, ARG_SITECODE_SEARCHTYPE,
				msgBuff, "Site Code", Tables.SITE.COL_SITE_CODE, clauses);
		addStringSearch(request, ARG_SITENAME, ARG_SITENAME_SEARCHTYPE, false,
				msgBuff, "Site Name", Tables.SITE.COL_SITE_NAME, clauses);

		// SITE_GROUP restriction //////////////////////////////////////////////////////////////////
		// TODO: since user has specified the site_type_code for us, we don't need to query
		// both congps and camgps.  remove the siteTypeMode clause.

		if (request.defined(ARG_SITE_GROUP)) {
			List<Clause> groupClauses = new ArrayList<Clause>();
			List<String> values = (List<String>) request.get(ARG_SITE_GROUP,
					new ArrayList());
			String col = null;
			int cnt = 0;
			if (   siteTypeMode.equals( Mode.SiteTableSiteType.CONGPS ) ) {

				col = Tables.SITE_AFFILIATION.COL_ARRAY_CODE;
				//Handle the 4 cases

				for (String group : values) {
					appendSearchCriteria(msgBuff, ((cnt++ == 0) ? "Site Group="
							: ""), group);
					groupClauses.add(Clause.eq(col, group));
					groupClauses.add(Clause.like(col,
							SqlUtil.wildCardBefore(", " + group)));
					groupClauses.add(Clause.like(col,
							SqlUtil.wildCardAfter(group + ",")));
					groupClauses.add(Clause.like(col,
							SqlUtil.wildCardBoth(", " + group + ",")));
				}
				clauses.add(Clause.or(groupClauses));

			} else if ( siteTypeMode.equals( Mode.SiteTableSiteType.CAMGPS ) ) {

				groupClauses = new ArrayList<Clause>();
				values = (List<String>) request.get(ARG_SITE_GROUP,
						new ArrayList());
				col = Tables.GEODETIC_CAMPAIGN.COL_NAME;
				//Handle the 4 cases
				cnt = 0;
				for (String group : values) {
					appendSearchCriteria(msgBuff, ((cnt++ == 0) ? "Site Group="
							: ""), group);
					groupClauses.add(Clause.eq(col, group));
					groupClauses.add(Clause.like(col,
							SqlUtil.wildCardBefore(", " + group)));
					groupClauses.add(Clause.like(col,
							SqlUtil.wildCardAfter(group + ",")));
					groupClauses.add(Clause.like(col,
							SqlUtil.wildCardBoth(", " + group + ",")));
				}
				clauses.add(Clause.or(groupClauses));
			}
		}

		// ADDITIONAL SITE TABLE SEARCH COLUMNS //////////////////////////////
		// these are set in searchColumns[] array
		for (String[] argValues : _searchColumns) {
			if (request.defined(argValues[0])) {
				System.err.println("search cols loop: argValues[1]: "
						+ argValues[1] + " argValues[2]: " + argValues[2]);
				args = (List<String>) request
						.getDelimiterSeparatedList(argValues[0]);
				clauses.add(Clause.or(Clause.makeStringClauses(argValues[1],
						args)));
				addSearchCriteria(msgBuff, argValues[2], args);
			}
		}

		/////////////////////////////////////////////////////////////////////////////////////////

		// for tectonic plate, add to sqlBetween*:
		// LEFT JOIN tectonic_plate tectonic_plate ON tectonic_plate.tectonic_plate_id = site.tectonic_plate_id
		// and add to clauses:
		// and (TECTONIC_PLATE.TECTONIC_PLATE_NAME = 'African')
		if (request.defined(GsacExtArgs.ARG_TECTONICPLATE)) { //////////// TECTONIC PLATE
			args = (List<String>) request
					.getDelimiterSeparatedList(GsacExtArgs.ARG_TECTONICPLATE);
			clauses.add(Clause.or(Clause.makeStringClauses(
					"tectonic_plate.tectonic_plate_name", args))); // column, value(s)
			addSearchCriteria(msgBuff, "Tectonic Plate", args); // name that appears
		}

		// TODO: create columns/table with current equipment info
		// for antenna CONGPS, add to sqlBetween*:
		// LEFT JOIN site_transaction_log on site_transaction_log.site_id = site.site_id
		// and add to clauses here:
		// and (SITE_TRANSACTION_LOG.SITE_TRANSACTION_TYPE = 'antenna / dome')
		// and (SITE_TRANSACTION_LOG.SITE_TRANSACTION_NAME = 'antenna model code')

		List<Clause> stlClauses = new ArrayList<Clause>();
		if (request.defined(GsacExtArgs.ARG_ANTENNA)) { //////////////// ANTENNA
			args = (List<String>) request
					.getDelimiterSeparatedList(GsacExtArgs.ARG_ANTENNA);
			if ( siteTypeMode.equals( Mode.SiteTableSiteType.CONGPS )) {

				//			stlClauses.add(Clause.eq(Tables.SITE_TRANSACTION_LOG.COL_SITE_TRANSACTION_VALUE, args));
				stlClauses.add(Clause.eq(
						Tables.SITE_TRANSACTION_LOG.COL_SITE_TRANSACTION_TYPE,
						"antenna / dome"));
				stlClauses.add(Clause.eq(
						Tables.SITE_TRANSACTION_LOG.COL_SITE_TRANSACTION_NAME,
						"antenna model code"));
				Clause stlClause = Clause.and(stlClauses);
				clauses.add(Clause.or(Clause.makeStringClauses(
						"SITE_TRANSACTION_LOG.SITE_TRANSACTION_VALUE", args))); // column, value(s)
				clauses.add(stlClause);
			} else if ( siteTypeMode.equals( Mode.SiteTableSiteType.CAMGPS )) {
				// AND (PGM.MANUFACTURED_EQUIPMENT.MODEL = ? ) AND (PGM.EQUIPMENT_TYPE.CATEGORY = 'gnss receiver' )
				args = (List<String>) request
						.getDelimiterSeparatedList(GsacExtArgs.ARG_ANTENNA);
				stlClauses.add(Clause.eq("PGM.EQUIPMENT_TYPE.CATEGORY",
						"GNSS Antenna"));
				Clause stlClause = Clause.and(stlClauses);
				clauses.add(Clause.or(Clause.makeStringClauses(
						"PGM.MANUFACTURED_EQUIPMENT.MODEL", args))); // column, value(s)
				clauses.add(stlClause);
			}
			addSearchCriteria(msgBuff, "Antenna", args); // name that appears
		} else if (request.defined(GsacExtArgs.ARG_RECEIVER)) { ///////////////// RECEIVER
			args = (List<String>) request
					.getDelimiterSeparatedList(GsacExtArgs.ARG_RECEIVER);
			if ( siteTypeMode.equals( Mode.SiteTableSiteType.CONGPS )) {

				stlClauses.add(Clause.eq(
						Tables.SITE_TRANSACTION_LOG.COL_SITE_TRANSACTION_TYPE,
						"receiver"));
				stlClauses.add(Clause.eq(
						Tables.SITE_TRANSACTION_LOG.COL_SITE_TRANSACTION_NAME,
						"model code"));
				Clause stlClause = Clause.and(stlClauses);
				clauses.add(Clause.or(Clause.makeStringClauses(
						"SITE_TRANSACTION_LOG.SITE_TRANSACTION_VALUE", args)));
				clauses.add(stlClause);
			} else if ( siteTypeMode.equals( Mode.SiteTableSiteType.CAMGPS )) {
				args = (List<String>) request
						.getDelimiterSeparatedList(GsacExtArgs.ARG_RECEIVER);
				stlClauses.add(Clause.eq("PGM.EQUIPMENT_TYPE.CATEGORY",
						"GNSS Receiver"));
				Clause stlClause = Clause.and(stlClauses);
				clauses.add(Clause.or(Clause.makeStringClauses(
						"PGM.MANUFACTURED_EQUIPMENT.MODEL", args))); // column, value(s)
				clauses.add(stlClause);
			}
			addSearchCriteria(msgBuff, "Antenna", args); // name that appears
		} else if (request.defined(GsacExtArgs.ARG_DOME)) { ///////////////// DOME
			args = (List<String>) request
					.getDelimiterSeparatedList(GsacExtArgs.ARG_DOME);
			if ( siteTypeMode.equals( Mode.SiteTableSiteType.CONGPS )) {

				stlClauses.add(Clause.eq(
						Tables.SITE_TRANSACTION_LOG.COL_SITE_TRANSACTION_TYPE,
						"antenna / dome"));
				stlClauses.add(Clause.eq(
						Tables.SITE_TRANSACTION_LOG.COL_SITE_TRANSACTION_NAME,
						"dome model code"));
				Clause stlClause = Clause.and(stlClauses);
				clauses.add(Clause.or(Clause.makeStringClauses(
						"SITE_TRANSACTION_LOG.SITE_TRANSACTION_VALUE", args)));
				clauses.add(stlClause);

			} else if ( siteTypeMode.equals( Mode.SiteTableSiteType.CAMGPS ) ) {
				stlClauses.add(Clause.eq("PGM.EQUIPMENT_TYPE.CATEGORY", "GNSS Radome"));
				Clause stlClause = Clause.and(stlClauses);
				clauses.add(Clause.or(Clause.makeStringClauses(
						"PGM.MANUFACTURED_EQUIPMENT.MODEL", args))); // column, value(s)
				clauses.add(stlClause);
			}
			addSearchCriteria(msgBuff, "Dome", args); // name that appears
		} else if (request.defined(GsacExtArgs.ARG_HAS_METPACK)) { //////////// MET PACKAGE
			
			args = (List<String>) request.getDelimiterSeparatedList(GsacExtArgs.ARG_HAS_METPACK);
			//System.err.println( "arg_has_metpack" );
			//for (String arg : args) {
			//	System.err.println( "arg: " + arg );
			//}

			if ( siteTypeMode.equals( Mode.SiteTableSiteType.CONGPS )) {
				if ( args.get(0).equals("true")) {
					stlClauses.add( Clause.isNotNull(Tables.SITE_TRANSACTION_LOG.COL_SITE_TRANSACTION_VALUE));
				} else {
					stlClauses.add( Clause.isNull(Tables.SITE_TRANSACTION_LOG.COL_SITE_TRANSACTION_VALUE));
				}
				stlClauses.add(Clause.eq(
						Tables.SITE_TRANSACTION_LOG.COL_SITE_TRANSACTION_TYPE, "temperature sensor"));
				stlClauses.add(Clause.eq(
						Tables.SITE_TRANSACTION_LOG.COL_SITE_TRANSACTION_NAME, "model code"));
				Clause stlClause = Clause.and(stlClauses);
				clauses.add(stlClause);

			} else if ( siteTypeMode.equals( Mode.SiteTableSiteType.CAMGPS )) {
				System.err.println( "CAMPGPS metpack" );
				// TODO: the PGM database does not appear to have any met package information,
				// unlike the ARCHIVE tables for CAMPAIGN.  So, the following insures that 
				// the search comes up empty.  I.e., no campaign sites have met data.
				if ( args.get(0).equals("true")) {
					stlClauses.add( Clause.isNull(Tables.SITE.COL_ID) );
				}				
				Clause stlClause = Clause.and(stlClauses);
				clauses.add(stlClause);
			}
			addSearchCriteria(msgBuff, "MetPack", args); // name that appears
			
		} else if (request.defined(GsacExtArgs.ARG_MONUMENT)) { ///////////////// MONUMENT
			args = (List<String>) request
					.getDelimiterSeparatedList(GsacExtArgs.ARG_MONUMENT);
			if ( siteTypeMode.equals(  Mode.SiteTableSiteType.CONGPS)) {
				stlClauses.add(Clause.eq(
						Tables.SITE_TRANSACTION_LOG.COL_SITE_TRANSACTION_TYPE,
						"site id / monument"));
				stlClauses.add(Clause.eq(
						Tables.SITE_TRANSACTION_LOG.COL_SITE_TRANSACTION_NAME,
						"monument description"));
				Clause stlClause = Clause.and(stlClauses);
				clauses.add(Clause.or(Clause.makeStringClauses(
						"SITE_TRANSACTION_LOG.SITE_TRANSACTION_VALUE", args))); // column, value(s)
				clauses.add(stlClause);
			} else if ( siteTypeMode.equals(Mode.SiteTableSiteType.CAMGPS)) {
				clauses.add(Clause.or(Clause.makeStringClauses(
						"PGM.MONUMENT_CLASS.NAME", args))); // column, value(s)
			}
			addSearchCriteria(msgBuff, "Monument Type", args); // name that appears
		}

		// how do we do this for stl table, and also for pgm.monument_class?
		//addStringSearch(request, GsacExtArgs.ARG_MONUMENT, ARG_MONUMENT_SEARCHTYPE,
		//		msgBuff, "Monument Type", Tables.SITE., clauses);

		return clauses;
	}

	
	/**
	 * Iterate on the query statement and create sites.
	 * Skip by the given offset and only process limit sites
	 *
	 * @param request the request
	 * @param response the response
	 * @param statement statement
	 * @param offset skip
	 * @param limit max number of sites to create
	 *
	 * @return count of how many sites were created
	 *
	 * @throws Exception On badness
	 */
	public int processStatement(GsacRequest request, GsacResponse response,
			Statement statement, int offset, int limit) throws Exception {
		long t1 = System.currentTimeMillis();
		SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement,
				offset, limit);

		// Get the request property
		String siteTypeProp = (String) request.getProperty( SITE_TYPE_MODE );
		//System.err.println( "processStatement: " + siteTypeProp );
		Mode.SiteTableSiteType siteTypeMode = Mode.SiteTableSiteType.CONGPS; // avoid warnings by initializing
		if ( siteTypeProp.equals(CONGPS) ) {
			siteTypeMode = Mode.SiteTableSiteType.CONGPS;
		} else if ( siteTypeProp.equals(CAMGPS) ) {
			siteTypeMode = Mode.SiteTableSiteType.CAMGPS;
		}

		if ( siteTypeMode.equals(Mode.SiteTableSiteType.CONGPS)) {
			//System.err.println("in processStatement() for CONGPS");
			//Set<String> monIdSet = new HashSet<String>();
			//			while (iter.getNext() != null) {
			ResultSet results;

			while ((results = iter.getNext()) != null) {
				String monId = iter.getResults().getString(1);
				response.addResource(makeSite(iter.getResults()));

				// pj: turn off paging.  we can get multiple results per site
				//if (!iter.countOK()) {
				//	response.setExceededLimit();
				//	break;
				//}
			}

			// close iter, not results; former was returned by query
			//results.close();
			iter.close();

			//System.err.println("close connection");
			getDatabaseManager().closeAndReleaseConnection(statement);
			long t2 = System.currentTimeMillis();
			System.err.println("read " + iter.getCount() + " CONGPS sites in "
					+ (t2 - t1) + "ms");
			return iter.getCount();
			//return monIdSet.size();
		}

		// campaign sites have multiple campaigns and muliple lat/lons returned, so we can't
		// use a hash set on the id like above.  put into hash map and set data before
		// calling new makeSite() method
		else if ( siteTypeMode.equals( Mode.SiteTableSiteType.CAMGPS)) {

			Map<String, SopacCamgpsSite> monIdMap = new HashMap<String, SopacCamgpsSite>();
			while (iter.getNext() != null) {
				String monId = iter.getResults().getString(1);
				if (!monIdMap.containsKey(monId)) {
					//System.err.println("adding site id: " + monId);
					SopacCamgpsSite scs = new SopacCamgpsSite();
					scs.setMetadata(iter.getResults());
					monIdMap.put(monId, scs);
					scs = null;
					//					response.addResource(makeSite(iter.getResults()));
				} else {
					SopacCamgpsSite scs = monIdMap.get(monId);
					scs.setMetadata(iter.getResults());
					monIdMap.put(monId, scs);
					scs = null;
				}
				//if (!iter.countOK()) {
				//	response.setExceededLimit();
				//	break;
				//}
			}

			iter.close();

			for (Map.Entry<String, SopacCamgpsSite> entry : monIdMap.entrySet()) {
				String key = entry.getKey();
				SopacCamgpsSite scs = entry.getValue();
				response.addResource(makeSite(scs));
			}

			getDatabaseManager().closeAndReleaseConnection(statement);
			long t2 = System.currentTimeMillis();
			System.err.println("read " + monIdMap.size() + " CAMGPS sites in "
							+ (t2 - t1) + "ms");
			return monIdMap.size();
		}

		return 0;
	}

	/**
	 *
	 * Create a single CONGPS site
	 *
	 * @param results db results
	 *
	 * @return the site
	 *
	 * @throws Exception on badness
	 */
	public GsacSite makeSite(ResultSet results) throws Exception {
		//System.err.println("makeSite: ");

		int colCnt = 1;
		GsacSite site = null;
		// assign variables from result set //////////////////////////

		String monId = results.getString(colCnt++);
		//System.err.println("makeSite: monId: " + monId);
		String fourCharId = results.getString(colCnt++);
		String name = results.getString(colCnt++);
		double latitude = results.getDouble(colCnt++);
		double longitude = results.getDouble(colCnt++);
		double elevation = results.getDouble(colCnt++);

		//System.err.println("makeSite: elev: " + elevation);

		//String type = results.getString(colCnt++);
		int startYear = results.getInt(colCnt++);
		int startDay = results.getInt(colCnt++);
		int endYear = results.getInt(colCnt++);
		int endDay = results.getInt(colCnt++);
		String arrayCode = results.getString(colCnt++);

		//String  status = results.getString(colCnt++);
		//String  groups = results.getString(colCnt++);

		//System.err.println("start day: " + startDay);
		//System.err.println("start year: " + startYear);
		//System.err.println("end day: " + endDay);
		//System.err.println("end year: " + endYear);

		Date startDate = null;
		Date endDate = null;
		//DateTime currentDate = null;
		DateTimeUtils dtu;
		int numDaysSinceData = 0;
		// modify variables, create new ones for output  ////////////////////////////
		if (startYear > 0) {
			dtu = new DateTimeUtils(startYear, startDay);
			startDate = dtu.getDate();
		}

		// set site status to active/inactive
		if (endYear > 0) {
			dtu = new DateTimeUtils(endYear, endDay);
			endDate = dtu.getDate();
			DateTime endDateTime = dtu.getDateTime();
			DateTime currentDateTime = new DateTime();
			dtu = new DateTimeUtils();
			numDaysSinceData = dtu.getDaysBetweenDates(endDateTime,
					currentDateTime);
			//System.err.println("num days since data: " + numDaysSinceData);
		}
		dtu = null;
		//System.err.println("start date: " + startDate.toString());
		//System.err.println("end date: " + endDate.toString());
		String type = "Continuous GPS/GNSS";
		String groups = arrayCode;

		// set the GsacSite object //////////////////////////////////////////////////
		site = new GsacSite("" + monId, fourCharId, name, latitude, longitude,
				elevation);
		site.setType(new ResourceType(type));
		site.setFromDate(startDate);
		site.setToDate(endDate);

		if ((groups != null) && (groups.trim().length() > 0)) {
			List<String> toks = new ArrayList<String>();
			for (String tok : groups.split(",")) {
				toks.add(tok.trim());
			}
			Collections.sort(toks);
			for (String tok : (List<String>) toks) {
				site.addResourceGroup(new ResourceGroup(tok));
			}
		}

		//Add icons based on type
		// TODO (see UnavcoSiteManager)
		
		// to add additional information to site summary page:
		
		//String siteLogText = "1.   Site Identification of the GNSS Monument\n     Site Name                : TresPiedraNM2006\n     Four Cha\racter ID        : P123\n     Monument Inscription     : ";
	   // site.addMetadata(new PropertyMetadata("siteloginfo",
		//	                                              siteLogText, "Site Log Info"));

		
		// to add equipment metadata to site summary page:
		//site.addMetadata(new GnssEquipment());
		
		/*
		 *  to get equipment info: perform queries for receiver and antenna.  see notes
		 *  in /shared/docs/projects/gsacws/gsacReleaseNotes.txt from 2/2/2011	
		 */
		
		
		MetadataGroup miscGroup = new MetadataGroup("Misc",
				MetadataGroup.DISPLAY_FORMTABLE);
		site.addMetadata(miscGroup);
		// TODO: add monument style

		miscGroup.addMetadata(new LinkMetadata(
				"ftp://sopac-ftp.ucsd.edu/pub/docs/site_logs/"
						+ fourCharId.toLowerCase() + ".log.txt", "Site log"));

		// TODO: set status to inactive if last day of data > 30 days old
		// can't search on this since its not a db column, but we can return that info
		// in site summary page

		String status = "Active";
		if (numDaysSinceData > 30)
			status = "Inactive";

		site.setStatus(new ResourceStatus(status));

		return site;

	}
	

	/**
	 * Create a single CAMGPS site using a SopacCamgpsSite object as input
	 * @param scs
	 * @return
	 */
	public GsacSite makeSite(SopacCamgpsSite scs) {

		//System.err.println("makeSite (campaign): begin");
		GsacSite site = null;
		String monId = scs.getMonId();
		String fourCharId = scs.getFourCharId();
		String name = scs.getName();
		double latitude = scs.getLat();
		double longitude = scs.getLon();
		double elevation = scs.getElev();

		//System.err.println("makeSite (campaign): elev: " + elevation);
		String type = "Campaign GPS/GNSS";
		String groups = scs.getGroups();

		//System.err.println("mon id: " + monId);

		Date startDate = scs.getMinStartDate();
		Date endDate = scs.getMaxEndDate();

		// set the GsacSite object //////////////////////////////////////////////////
		//System.err.println("makeSite(camgps): setting new gsac site object, mon id: " + monId);
		site = new GsacSite("" + monId, fourCharId, name, latitude, longitude,
				elevation);
		site.setType(new ResourceType(type));
		site.setFromDate(startDate);
		site.setToDate(endDate);

		if ((groups != null) && (groups.trim().length() > 0)) {
			List<String> toks = new ArrayList<String>();
			for (String tok : groups.split(",")) {
				toks.add(tok.trim());
			}
			Collections.sort(toks);
			for (String tok : (List<String>) toks) {
				site.addResourceGroup(new ResourceGroup(tok));
			}
		}

		// TODO: create icons

		return site;
	}
	
	/**
	 *
	 * Get the site from the database
	 *
	 * This is called when querying for basic info for a single site.
	 *
	 * @param siteId site id. This isn't the site code but actually the monument id
	 *
	 * @return the site or null if not found
	 *
	 * @throws Exception on badness
	 */

	public GsacResource getResource(String siteId) throws Exception {

		List<String> tableNames = new ArrayList<String>();
		List<Clause> clauses = new ArrayList<Clause>();

		String sqlBetweenFromAndWhere;
		Statement statement;

		// perform query for congps sites
		tableNames.add(Tables.SITE.NAME);
		clauses.add(Clause.eq(Tables.SITE.COL_SITE_ID, siteId));
		clauses.add(Clause.eq(Tables.SITE.COL_SITE_TYPE_CODE, "CONGPS"));
		Clause mainClause = Clause.and(clauses);

		sqlBetweenFromAndWhere = "LEFT JOIN SITE_DATA SITE_DATA ON "
				+ "SITE.SITE_ID = SITE_DATA.SITE_ID "
				+ "LEFT JOIN SITE_AFFILIATION SITE_AFFILIATION ON "
				+ "SITE.SITE_ID = SITE_AFFILIATION.SITE_ID "
				+ "INNER JOIN SITE_COORDINATES_GEODETIC SITE_COORDINATES_GEODETIC  ON "
				+ "SITE.SITE_ID = SITE_COORDINATES_GEODETIC.SITE_ID  "
				// pj, 12/08/2010: use mean coords source restriction below to get single llh/site	
				+ "INNER JOIN COORDINATE_SOURCE COORDINATE_SOURCE  ON "
				+ "COORDINATE_SOURCE.SOURCE_ID = SITE_COORDINATES_GEODETIC.SOURCE_ID  ";

		statement = getDatabaseManager().select(getCongpsSiteSelectColumns(),
				tableNames, mainClause, sqlBetweenFromAndWhere, null, -1);

		SqlUtil.Iterator iter = getDatabaseManager().getIterator(statement);
		Set<String> monIdSet = new HashSet<String>();
		//			while (iter.getNext() != null) {
		ResultSet results;

		try {			
			results = statement.getResultSet();
			if (!results.next()) { // try camgps
				//System.err.println("getSite(): no results exist, try CONGPS");
				results.close();
				
				// need to close the previous statement, since we are creating a 
				// new one below.
				getDatabaseManager().closeAndReleaseConnection(statement);
				
				clauses = new ArrayList<Clause>();
				clauses.add(Clause.eq(Tables.SITE.COL_SITE_ID, siteId));
				clauses.add(Clause.eq(Tables.SITE.COL_SITE_TYPE_CODE, "CAMGPS"));
				mainClause = Clause.and(clauses);
				tableNames = new ArrayList<String>();
				sqlBetweenFromAndWhere = 
						  "INNER JOIN  site_coordinates_geodetic site_coordinates_geodetic ON "
						+ "site.site_id = site_coordinates_geodetic.site_id "
						+ "INNER JOIN campaign_monument campaign_monument ON "
						+ "site.site_id = campaign_monument.monument_id "
						+ "LEFT JOIN geodetic_campaign geodetic_campaign ON "
						+ "geodetic_campaign.id = campaign_monument.geodetic_campaign_id "
						+ "LEFT JOIN PGM.campaign_monument_profile campaign_monument_profile ON "
						+ "campaign_monument.id = PGM.campaign_monument_profile.campaign_monument_id "
						+ "INNER JOIN COORDINATE_SOURCE COORDINATE_SOURCE  ON "
						+ "COORDINATE_SOURCE.SOURCE_ID = SITE_COORDINATES_GEODETIC.SOURCE_ID  ";
				tableNames.add(Tables.SITE.NAME);

				statement = getDatabaseManager().select(
						getCamgpsSiteSelectColumns(), tableNames, mainClause,
						sqlBetweenFromAndWhere, CAMGPS_SITE_GROUP_BY, -1);
				iter = getDatabaseManager().getIterator(statement);
				SopacCamgpsSite scs = new SopacCamgpsSite();
				while (iter.getNext() != null) {
					scs.setMetadata(iter.getResults());
				}

				iter.close();
				GsacSite site = makeSite(scs);
				
				// finally() will call getDatabaseManager().closeAndReleaseConnection(statement);
				return site;
			} else { // we got CONGPS
				GsacSite site = makeSite(results);				
				results.close();
				return site;
			}
		} finally {
			//System.err.println("getSite(): close connection");
			getDatabaseManager().closeAndReleaseConnection(statement);
		}
	}

	

	/**
	 * Get the site groups in the CAMGPS tables
	 */

	// TODO: create SOPAC version of site.group.properties and add a addCapabilities
	// statement to makeCapabilitie() below.  remove this method.
	public List<ResourceGroup> doGetResourceGroups()  {
		//System.err.println("SopacSiteManager.doGetResourceGroups(): ");

		Statement statement = null;
		try {
			HashSet<String> seen = new HashSet<String>();
			List<ResourceGroup> groups = new ArrayList<ResourceGroup>();

			// can't set table to just "Table.GEODETIC_CAMPAIGN"

			statement = getDatabaseManager().select(
					distinct("GEODETIC_CAMPAIGN.NAME"), "GEODETIC_CAMPAIGN");
			List<Clause> clauses = new ArrayList<Clause>();
			List<String> tableNames = new ArrayList<String>();
			//tableNames.add(Tables.GEODETIC_CAMPAIGN.COL_ID);

			//System.err.println("select stmt 5");
			
			for (String commaDelimitedList : SqlUtil.readString(
					getDatabaseManager().getIterator(statement), 1)) {
				if (commaDelimitedList == null) {
					continue;
				}
				for (String tok : commaDelimitedList.split(",")) {
					tok = tok.trim();
					if (seen.contains(tok)) {
						continue;
					}
					seen.add(tok);
					groups.add(new ResourceGroup(tok));
				}
			}

			//CONGPS

			//System.err.println("select stmt 6");
			
			getDatabaseManager().closeAndReleaseConnection(statement);
			statement = getDatabaseManager()
					.select(
					//distinct(Tables.GEODETIC_CAMPAIGN.COL_NAME),
					//							 Tables.GEODETIC_CAMPAIGN.COL_ID);
					distinct("SITE_AFFILIATION.ARRAY_CODE"), "SITE_AFFILIATION");
			for (String commaDelimitedList : SqlUtil.readString(
					getDatabaseManager().getIterator(statement), 1)) {
				if (commaDelimitedList == null) {
					continue;
				}
				for (String tok : commaDelimitedList.split(",")) {
					tok = tok.trim();
					if (seen.contains(tok)) {
						continue;
					}
					seen.add(tok);
					groups.add(new ResourceGroup(tok));
				}
			}

			Collections.sort((List<ResourceGroup>) groups);

			//System.err.println("SopacSiteManager.doGetResourceGroups(): end");

			return groups;
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		} finally {
			try {
			getDatabaseManager().closeAndReleaseConnection(statement);
			} catch (java.sql.SQLException sqle) {
				throw new RuntimeException (sqle);
			}
		}
	}

	
	/**
	 * Get the extra site search capabilities. This
	 * calls makeCapabilities to actually make them
	 *
	 * @return site search capabilities
	 */
	public List<Capability> doGetQueryCapabilities() {
		try {
			List<Capability> capabilities = new ArrayList<Capability>();
			// pj, 12/03/2010 per jeff merging capabilities functions.  needs to
			// go before makeCapabilities call so regular site search form appears
			// before advanced search
			addDefaultCapabilities(capabilities);
			makeCapabilities(capabilities);
			return capabilities;
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}

	
	/**
	 * Makes the extra site search capabilities
	 *
	 * @param capabilities List to add to
	 *
	 * @throws Exception On badness
	 */
	private void makeCapabilities(List<Capability> capabilities)
			throws Exception {
		
		/*
		 * these populate the drop down boxes under "advanced" in site search form, or the lists
		 * on the Browse pages (e.g., antennas).  uses the searchColumns[] array, or new capabilities.add statements that use similar args
		 * TODO: "has met pack", "sample interval", and "data access policy" (and in getSiteClauses() above)
		 * 
		 * When adding a new capability below (e.g., antenna), it uses the corresponding .properties file 
		 * (e.g., site.antenna.properties) value in the GSAC form.  It uses the corresponding value for the same key
		 * (where file contents are key=value) in site.antenna.map to query the db. 
		 * TODO: create site.group.[map|properties] files from the SITE_AFFILIATION.ARRAY_CODE and 
		 * PGM.GEODETIC_CAMPAIGN.ARRAY_CODE column contents, replace doGetResourceGroups() method, add a capabilities statement for it below.
		*/
		
		Statement statement = null;
		String[] values;
		List<Clause> clauses = new ArrayList<Clause>();
		Clause mainClause;
		List<String> tableNames = new ArrayList<String>();

		// sample searchColumn
		//   { GsacExtArgs.ARG_STATE, Tables.SITE.COL_STATE, "State" } };

		tableNames.add(Tables.SITE.NAME);
		tableNames.add(Tables.SITE_COORDINATES_GEODETIC.NAME);
		Clause monumentJoin = null;
		monumentJoin = Clause.join(Tables.SITE.COL_SITE_ID,
				Tables.SITE_COORDINATES_GEODETIC.COL_SITE_ID);
		clauses.add(monumentJoin);
		mainClause = Clause.and(clauses);

		try {
			for (String[] tuple : _searchColumns) {
	
				//			statement = getDatabaseManager().select(distinct(tuple[1]), "SITE");
				//System.err.println("select stmt 7");
				statement = getDatabaseManager().select(distinct(tuple[1]),
						tableNames, mainClause);			
				values = SqlUtil.readString(
						getDatabaseManager().getIterator(statement), 1);
				Arrays.sort(values);
				Capability cap =   new Capability(tuple[0], tuple[2], values, true);
				cap.setGroup(SiteManager.CAPABILITY_GROUP_ADVANCED);
				capabilities.add(cap);
				getDatabaseManager().closeAndReleaseConnection(statement);
			}
			
			// TODO: determine if possible since site_transaction_log not normal
			// modified date /////////////////
		    //capabilities.add(initCapability(new Capability(ARG_SITE_MODIFYDATE,
	        //        "Site Modified Date Range",
	        //        Capability.TYPE_DATERANGE), CAPABILITY_GROUP_ADVANCED,
	        //            "The site's metadata was modified between these dates"));
			
			// TODO: Determine if possible since site.date_inserted has many nulls
			// creation date //////////////////////
		    //capabilities.add(
	        //        initCapability(
	        //            new Capability(
	        //                ARG_SITE_CREATEDATE, "Site Created Date Range",
	        //                Capability.TYPE_DATERANGE), CAPABILITY_GROUP_ADVANCED,
	        //                    "The site was created between these dates"));
	
			// antenna list ///////////////////////////////////////////
			Capability cap = new Capability(GsacExtArgs.ARG_ANTENNA, "Antenna", makeVocabulary(GsacExtArgs.ARG_ANTENNA), true);
			cap.setGroup(SiteManager.CAPABILITY_GROUP_ADVANCED);
			capabilities.add(cap);
			
			//		 receiver list ///////////////////////////////////////////
			cap = new Capability(GsacExtArgs.ARG_RECEIVER, "Receiver", makeVocabulary(GsacExtArgs.ARG_RECEIVER), true);
			cap.setGroup(SiteManager.CAPABILITY_GROUP_ADVANCED);
			capabilities.add(cap);
	
			//		 dome list ///////////////////////////////////////////
			cap = new Capability(GsacExtArgs.ARG_DOME, "Dome", makeVocabulary(GsacExtArgs.ARG_DOME), true);
			cap.setGroup(SiteManager.CAPABILITY_GROUP_ADVANCED);
			capabilities.add(cap);
	
			//		 monument description ///////////////////////////////////////////
			// TODO: add various monument descriptions as comma-separated values to 
			// site.monument.map
			// TODO: add monuments from PGM.MONUMENT_CLASS table
			// TODO: change to monument description (from style) in gsac form?
			cap = new Capability(GsacExtArgs.ARG_MONUMENT, "Monument Style", makeVocabulary(GsacExtArgs.ARG_MONUMENT), true);		
			cap.setGroup(SiteManager.CAPABILITY_GROUP_ADVANCED);
			capabilities.add(cap);
			
			//		 tectonic plate name ///////////////////////////////////////////
			cap = new Capability(GsacExtArgs.ARG_TECTONICPLATE, "Tectonic Plate", makeVocabulary(GsacExtArgs.ARG_TECTONICPLATE), true);
			cap.setGroup(SiteManager.CAPABILITY_GROUP_ADVANCED);
			capabilities.add(cap);
			
			//		Metrology Pack //////////////////////////////////////
	        capabilities.add(new Capability(GsacExtArgs.ARG_HAS_METPACK,
	                "Has Met Pack",
	                Capability.TYPE_BOOLEAN,
	                CAPABILITY_GROUP_ADVANCED));
					
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		} finally {
			try {
			getDatabaseManager().closeAndReleaseConnection(statement);
			} catch (java.sql.SQLException sqle) {
				throw new RuntimeException (sqle);
			}
		}
	}

	/**
	 * Get the CONGPS columns that are to be searched on
	 *
	 * @param request the request
	 *
	 * @return comma delimited fully qualified column names to select on
	 */
	public String getCongpsSiteSelectColumns() {
		return CONGPS_SITE_WHAT;
	}

	/**
	 * Get the CAMGPS columns that are to be searched on
	 *
	 * @param request the request
	 *
	 * @return comma delimited fully qualified column names to select on
	 */
	public String getCamgpsSiteSelectColumns() {
		return CAMGPS_SITE_WHAT;
	}

	/**
	 * Get the order by clause
	 *
	 * @param request the request
	 *
	 * @return order by clause
	 */
	public String getSiteOrder(GsacRequest request) {

		if (request == null || !request.defined(ARG_SITE_SORT_VALUE)) {
			return SITE_ORDER;
		}
		boolean ascending = request.getSiteAscending();
		StringBuffer cols = new StringBuffer();
		//TODO: Implement sorting order code
		for (String sort : request.getDelimiterSeparatedList(ARG_SITE_SORT_VALUE)) {
			String col = null;
			if (sort.equals(SORT_SITE_CODE)) {
				//                col = Tables.MV_DAI_PRO.COL_MON_SITE_CODE;
			} else if (sort.equals(SORT_SITE_NAME)) {
				//                col = Tables.MV_DAI_PRO.COL_MON_SITE_NAME;
			} else if (sort.equals(SORT_SITE_TYPE)) {
				//                col = Tables.MV_DAI_PRO.COL_SITE_TYPE;
			}
			if (col != null) {
				if (cols.length() != 0)
					cols.append(",");
				//Oracle has a UPPER operator. We use this to sort on upper case
				cols.append("UPPER(" + col + ")");
			}
		}
		if (cols.length() > 0) {
			return orderBy(cols.toString(), ascending);
		}
		return SITE_ORDER;
	}

	public String getSiteSelectSuffix(GsacRequest request) {
		//return super.getSiteSelectSuffix(request);
		//At unavco we end up doing a group by on our site select
		//Something like:
		//	return SITE_GROUP_BY+(request!=null?" " +getSiteOrder(request):"") ;

		// This can get called by FileManager and the request will be null,
		// but the mode should be CONGPS
		if (request == null) {
			return super.getSiteSelectSuffix(request);
		}
		
		String siteTypeMode = (String) request.getProperty( SITE_TYPE_MODE );
		//System.err.println("siteTypeMode: " + siteTypeMode );
		if ( siteTypeMode.equals( CONGPS ) ) {
			return super.getSiteSelectSuffix(request);
		} else if ( siteTypeMode.equals( CAMGPS )) {
			System.err.println("returning group by");
			return CAMGPS_SITE_GROUP_BY
					+ (request != null ? " " + getSiteOrder(request) : "");
			//return super.getSiteSelectSuffix(request);
		}
		return null;
	}

	/**
	 * Get the site from the database.
	 * This looks up the site using
	 * the monument id
	 *
	 * @param permanentSiteId
	 *
	 * @return the site or null if not found
	 *
	 * @throws Exception On badness
	 */
	public GsacSite getSiteForResource(String siteId) throws Exception {
		//System.err.println("getSiteForResource: siteId: " + siteId);
		GsacSite site = exportSiteCache.get(siteId + "");
		if (site != null) {
			return site;
		}

		List<String> tableNames = new ArrayList<String>();
		String sqlBetweenFromAndWhere;
		Statement statement;
		tableNames.add(Tables.SITE.NAME);

		StringBuffer sb = new StringBuffer();
		sb.append(
			"LEFT JOIN SITE_DATA SITE_DATA ON SITE.SITE_ID = SITE_DATA.SITE_ID " +
			"LEFT JOIN SITE_AFFILIATION SITE_AFFILIATION ON SITE.SITE_ID = SITE_AFFILIATION.SITE_ID " +
			"INNER JOIN SITE_COORDINATES_GEODETIC SITE_COORDINATES_GEODETIC ON SITE.SITE_ID = SITE_COORDINATES_GEODETIC.SITE_ID  ");

		sqlBetweenFromAndWhere = sb.toString();
		
		System.err.println("sqlBetween...:" + sqlBetweenFromAndWhere );

		//System.err.println("select stmt 14");
		statement = getDatabaseManager().select(
				getCongpsSiteSelectColumns(),
				tableNames, Clause.eq(Tables.SITE.COL_SITE_ID, siteId),
				sqlBetweenFromAndWhere, getSiteSelectSuffix(null), -1);

//		Statement locQuery = getDatabaseManager().select(
//				"SITE.COUNTRY," + "SITE.STATE," + "SITE.COUNTY," + "SITE.CITY",
//				Tables.SITE.NAME, Clause.eq( Tables.SITE.COL_SITE_ID, site_id) ); 

		
		try {
			ResultSet results = statement.getResultSet();
			//check if we got a result
			if (!results.next()) {
				results.close();
				return null;
			}

			Set<String> monIdSet = new HashSet<String>();
			while (results.next()) {
				String monId = results.getString(1);
				if (!monIdSet.contains(monId)) {
					System.err.println("adding site id: " + monId);
					monIdSet.add(monId);
					//					response.addResource(makeSite(iter.getResults()));
					site = makeSite(results);
				}
			}
			monIdSet = null;
			results.close();
			//Cache the result
			if (site != null) {
				System.err.println("put site in cache: " + siteId + " lat: " + site.getLatitude());
				exportSiteCache.put("" + siteId, site);
			}
			return site;
		} finally {

			//System.err.println("close connection");
			getDatabaseManager().closeAndReleaseConnection(statement);
		}
	}

	
    /**
     * get all of the metadata for the given resource
     *
     * @param gsacResource The resource to set metadata on
     *
     * @throws Exception On badness
     */
    public void doGetFullMetadata(GsacResource gsacResource)
            throws Exception {
    	
    	String site_id = gsacResource.getId();
    	//System.err.println("doGetFullMetadata");
    	//System.err.println("GsacResource, resourceId: " + site_id );

		Statement locQuery = getDatabaseManager().select(
				"SITE.COUNTRY," + "SITE.STATE," + "SITE.COUNTY," + "SITE.CITY",
				Tables.SITE.NAME, Clause.eq( Tables.SITE.COL_SITE_ID, site_id) ); 

		try {
            ResultSet results = locQuery.getResultSet();
        	String country = null;
        	String state = null;
        	//String county = null;
        	String city = null;
        	// TODO: lists of resources....
        	// Presume only one, but this will get the last.
            while (results.next()) {
            	country = results.getString( Tables.SITE.ORA_COUNTRY );
            	state = results.getString( Tables.SITE.ORA_STATE );
            	//county = results.getString( Tables.SITE.ORA_COUNTY );
            	city = results.getString( Tables.SITE.ORA_CITY );
            }
    		// just a test
    		PoliticalLocationMetadata pl = new PoliticalLocationMetadata( country, state, city );
    		gsacResource.addMetadata( pl );
		} finally {
			getDatabaseManager().closeAndReleaseConnection(locQuery);
		}
		
		List<Clause> clauses = new ArrayList<Clause>();
		List<String> tableNames = new ArrayList<String>();
		tableNames.add(Tables.SITE_TRANSACTION_LOG.NAME);
		clauses.add( Clause.eq( Tables.SITE_TRANSACTION_LOG.COL_SITE_ID, site_id ) );
		clauses.add( Clause.or( 
				Clause.eq(Tables.SITE_TRANSACTION_LOG.COL_SITE_TRANSACTION_TYPE, "receiver"),
				Clause.eq(Tables.SITE_TRANSACTION_LOG.COL_SITE_TRANSACTION_TYPE, "antenna / dome") ));
		Clause clause = Clause.and( clauses );
		
		Statement statement = getDatabaseManager().select(
				"SITE_TRANSACTION_LOG.SITE_TRANSACTION_TYPE," +
				"SITE_TRANSACTION_LOG.SITE_TRANSACTION_NAME," +
				"SITE_TRANSACTION_LOG.SITE_TRANSACTION_VALUE," +
				"SITE_TRANSACTION_LOG.EFFECTIVE_DATE",
			clause.getTableNames(tableNames), clause,
			"order by " + Tables.SITE_TRANSACTION_LOG.COL_EFFECTIVE_DATE + " asc" , -1);

		String rType = null;		// 'model code'
		String rSN = null;			// 'serial number'
		String rFV = null;			// 'firmware version'
		String aType = null;		// 'antenna model code'
		String aHeight = null;		// 'antenna height'
		String aSN = null;			// 'antenna serial number'
		String dType = null;		// 'dome model code'
		String dSN = null;			// none
		//String fv = null;			// 'firmware version'
		//String ecs = null;			// 'elevation cutoff setting'
		//String ts = null;			// 'temperature stablilization'
		//String notes = null;		// 'additional information'
		Date lastDate = null;

		try {
            ResultSet results = statement.getResultSet();
            GnssEquipmentGroup equipmentGroup = null;
            while (results.next()) {

            	Date curDate = results.getDate( 4 ); 
            	//System.err.println( "endDate: " + eDate );
            	// Transactions are grouped by date, but ignore time part
            	// as there may be duplicates for the same day.
            	
            	if ((lastDate != null) && (lastDate.compareTo( curDate) != 0) ) {
            		// Mark a new entry
            		//System.err.println( "rType:" + rType + " );
            		double height = 0.0;
            		try {
            			height = Double.parseDouble( aHeight );
            			
            		} finally { }
            		Date[] dateRange = { lastDate, curDate  };
            		if ( equipmentGroup == null ) {
            			gsacResource.addMetadata(equipmentGroup = new GnssEquipmentGroup());
            		}
            		//System.err.println( "rType:" + rType + " sn: " + sn + " fv: " + fv + " ts: " + ts + " notes: " + notes );
            		equipmentGroup.addMetadata( new GnssEquipment(dateRange,
                            aType, aSN, dType, dSN, rType, rSN, rFV, height )); 
            	}
            	lastDate = curDate;

            	//String type = results.getString(1);
            	String name = results.getString(2);
            	String value = results.getString(3);

            	if (name.equals("model code")) 				rType = value; 
                if (name.equals("serial number")) 			rSN = value;
                if (name.equals("firmware version")) 		rFV = value;
            	if (name.equals("antenna model code")) 		aType = value; 
            	if (name.equals("antenna serial number")) 	aSN = value;
            	if (name.equals("antenna height"))			aHeight = value;
            	if (name.equals("dome model code"))			dType = value;
                //if (name.equals("firmware version")) 		fv = value;
                //if (name.equals("elevation cutoff setting")) ecs = value;
                //if (name.equals("temperature setting")) 	ts = value;
                //if (name.equals("additional information")) notes = value;

            }
            
		
		} finally {
			getDatabaseManager().closeAndReleaseConnection(statement);
		}

        // Add real time stream metadata. 

		clauses = new ArrayList<Clause>();
		//List<String> tableNames = new ArrayList<String>();
		//tableNames.add(Tables.SITE_TRANSACTION_LOG.NAME);
		clauses.add( Clause.eq( Tables.SITE_TRANSACTION_LOG.COL_SITE_ID, site_id ) );
		clauses.add(	Clause.eq(Tables.SITE_TRANSACTION_LOG.COL_SITE_TRANSACTION_TYPE, "realtime") );
		Clause rtClause = Clause.and( clauses );
		
		Statement rtStatement = getDatabaseManager().select(
				"SITE_TRANSACTION_LOG.SITE_TRANSACTION_TYPE," +
				"SITE_TRANSACTION_LOG.SITE_TRANSACTION_NAME," +
				"SITE_TRANSACTION_LOG.SITE_TRANSACTION_VALUE," +
				"SITE_TRANSACTION_LOG.EFFECTIVE_DATE",
			clause.getTableNames(tableNames), rtClause,
			"order by " + Tables.SITE_TRANSACTION_LOG.COL_EFFECTIVE_DATE + " asc" , -1);

		lastDate = null;
		
		// NTRIP medatata variables
		//String mountPoint;
		//String urlRoot; 
		//String id;
		//String format;
		//String formatDetails;
		//String carrier;
		//String navSystem;
		//String network;
		//String country;
		//double lat;
		//double lon;
		//int nmea;
		//int solution;
		//String generator;
		//String compression;
		//String auth;
		//String fee;
		//
		int bitRate;

		String ssh = null;
		String ssp = null;
		String ssf = null;
		String psh2 = null;
		String psp2 = null;
		String psf2 = null;
		String psh3 = null;
		String psp3 = null;;
		String psf3 = null;

		try {
            ResultSet results = rtStatement.getResultSet();
            GnssStreamGroup streamGroup = null;
            while (results.next()) {

            	if ( streamGroup == null ) {
        			gsacResource.addMetadata( streamGroup = new GnssStreamGroup() );
        		}
            	
            	//String type = results.getString(1);
            	String name = results.getString(2);
            	String value = results.getString(3);
            	Date curDate = results.getDate(4); 
            	//System.err.println( "name:  " + name );
            	//System.err.println( "value: " + value );

            	// There should be only one realtime group, but if not then 
            	// changes are cumulative like other transactions.

            	// The following are not currently in use.
            	//if (name.equals("site stream host")) 			ssh = value; 
                //if (name.equals("site stream port")) 			ssp = value;
                //if (name.equals("site stream format")) 		ssf = value;
            	if (name.equals("raw stream host")) 			ssh = value; 
                if (name.equals("raw stream port")) 			ssp = value;
                if (name.equals("raw stream format")) 			ssf = value;
            	if (name.equals("published stream host 2")) 	psh2 = value; 
            	if (name.equals("published stream port 2")) 	psp2 = value;
            	if (name.equals("published stream format 2"))	psf2 = value;
            	if (name.equals("published stream host 3"))		psh3 = value;
            	if (name.equals("published stream port 3")) 	psp3 = value;
            	if (name.equals("published stream format 3"))	psf3 = value;
            	
             	lastDate = curDate;
            }

            // If there are values assigned to the site stream or published streams, 
            // then create a StreamMetadata for them. Currently, the DB and SIM only
            // provide slots for three streams.
            if (ssh != null) {
    			StreamMetadata stream = new StreamMetadata();
    			stream.setType( StreamMetadata.TYPE_SITE);
    			stream.setUrl( ssh.trim() + ":" + ssp );
    			stream.setFormat(ssf);
    			stream.setBitRate(1);
    			streamGroup.addMetadata(stream);
            }

            if (psh2 != null) {
    			StreamMetadata stream = new StreamMetadata();
    			stream.setType( StreamMetadata.TYPE_PUBLISHED);
    			stream.setUrl( psh2.trim() + ":" + psp2 );
    			stream.setFormat(psf2);
    			stream.setBitRate(1);
    			streamGroup.addMetadata(stream);
            }
            
            if (psh3 != null) {
    			StreamMetadata stream = new StreamMetadata();
            	stream.setType( StreamMetadata.TYPE_PUBLISHED);
    			stream.setUrl( psh3.trim() + ":" + psp3 );
    			stream.setFormat(psf3);
    			stream.setBitRate(1);
    			streamGroup.addMetadata(stream);
            }
            
		} finally {
			getDatabaseManager().closeAndReleaseConnection(statement);
		}
		
    }
	
	
    /**
     * 
     * @author hankr
     *
     */
	public static class Mode {
		public enum SiteTableSiteType {
			CONGPS, CAMGPS
		};
	}
	
}
