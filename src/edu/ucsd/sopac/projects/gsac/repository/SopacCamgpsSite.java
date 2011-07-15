package edu.ucsd.sopac.projects.gsac.repository;

import java.sql.*;
import java.util.*;

public class SopacCamgpsSite {

	public SopacCamgpsSite() {
	}

	public void setMetadata(ResultSet results) throws SQLException {

		int colCnt = 1;
		_monId = results.getString(colCnt++);
		_fourCharId = results.getString(colCnt++);
		_name = results.getString(colCnt++);

		if (_latitude == -999.9) { // only set llh once
			_latitude = results.getDouble(colCnt++);
			_longitude = results.getDouble(colCnt++);
			_elevation = results.getDouble(colCnt++);
		} else {
			colCnt = colCnt + 3;
		}

		_startDate = results.getDate(colCnt++);
		if (!(_startDate == null)) {

			//System.err.println("start date: " + _startDate);
			if (_minStartDate == null || _minStartDate.after(_startDate)) {
				_minStartDate = _startDate;
				//System.err.println("new min start date: " + _minStartDate);
			}
		}
		_endDate = results.getDate(colCnt++);
		if (!(_endDate == null)) {
			if (_maxEndDate == null || _maxEndDate.before(_endDate)) {
				_maxEndDate = _endDate;
			}
		}

		_group = results.getString(colCnt++); // campaign name
		//System.err.println("setMetadata: group: " + _group);
		if (!(_group == null))
			setGroups();
	}

	private void setGroups() {
		_groupSet.add(_group);
	}

	public String getMonId() {
		return _monId;
	}

	public String getFourCharId() {
		return _fourCharId;
	}

	public String getName() {
		return _name;
	}

	public double getLat() {
		return _latitude;
	}

	public double getLon() {
		return _longitude;
	}

	public double getElev() {
		return _elevation;
	}

	// return a comma-separated list of campaigns
	public String getGroups() {
		String groups = null;
		if (_groupSet.size() > 0) {
			StringBuffer sb = new StringBuffer();
			for (String s : _groupSet) {
				//System.err
				//		.println("set metadata: group list loop: group: " + s);
				sb.append(s).append(",");
			}
			int lastIdx = sb.length() - 1;
			groups = sb.substring(0, lastIdx).toString();
		}
		return groups;
	}

	public java.util.Date getMinStartDate() {
		return _minStartDate;
	}

	public java.util.Date getMaxEndDate() {
		return _maxEndDate;
	}

	Set<String> _groupSet = new HashSet<String>();

	String _monId;

	String _fourCharId;

	String _name;

	double _latitude = -999.9;

	double _longitude = -999.9;

	double _elevation = -999.9;

	String _type = "CAMGPS";

	String _group;

	java.util.Date _minStartDate = null;

	java.util.Date _maxEndDate = null;

	java.util.Date _startDate = null;

	java.util.Date _endDate = null;
}
