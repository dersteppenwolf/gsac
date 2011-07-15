package edu.ucsd.sopac.projects.gsac.repository.utils;

import java.util.Date;
import org.joda.time.*;

public class DateTimeUtils {

	public DateTimeUtils(int year, int dayOfYear) {
		_iYear = year;
		_iDayOfYear = dayOfYear;
		setMonthDay();
		setDate();
	}
	
	public DateTimeUtils() {
	}
	
	private void setMonthDay() {
		int leapi = 0;
		int i = 0;
		// see if it is a leap year
		if ((_iYear - 1904) % 4 == 0) {
			leapi = 1;
		}
		for (i = 0; _iDayOfYear > months[leapi][i]; i++) {
			//System.err.println("i: " + i + " doy: " + _iDayOfYear + " month: " + months[leapi][i]);
			_iDayOfYear -= months[leapi][i];
		}
		//System.err.println("month: " + i + " day: " + _iDayOfYear);
		_iMonth = i;
		_iDay = _iDayOfYear;
	}

	public void setDate() {
		//System.err.println("year: " + _iYear + " month: " +  _iMonth + " day: " +
		//		_iDay + " hour: " + _iHour + " :min: " + _iMin + " sec: "+
		//		_iSec + " msec: " + _iMsec);
		_dt = new DateTime(_iYear, _iMonth, _iDay, _iHour, _iMin,
				_iSec, _iMsec);
		_date = _dt.toDate();
	}

	public Date getDate() {
		return _date;
	}

	public DateTime getDateTime() {
		return _dt;
	}
	
	public int getDaysBetweenDates (DateTime dt1, DateTime dt2) {
		Days d = Days.daysBetween(dt1, dt2);
		int days = d.getDays();
		return days;
	}
	
	int _iYear = 0;

	int _iDay = 0;

	int _iDayOfYear = 0;

	int _iMonth = 0;

	int _iHour = 0;

	int _iSec = 0;

	int _iMsec = 0;

	int _iMin = 0;

	Date _date;
	
	DateTime _dt;
	

	int months[][] = { { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 },
			{ 0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 } };

}
