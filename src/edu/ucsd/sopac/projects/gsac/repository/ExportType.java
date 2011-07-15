package edu.ucsd.sopac.projects.gsac.repository;

public class ExportType {
	public ExportType(int id, String name) {
		_id = id;
		_name = name;
	}

	public static final ExportType TYPE_RINEX_OBS = new ExportType(1, "RINEX");

    /** _more_          */
    public static final ExportType TYPE_RINEX_NAV = new ExportType(2,
                                                        "RINEX Navigation");

    /** _more_          */
    public static final ExportType TYPE_RINEX_MET = new ExportType(3,
                                                        "RINEX Meteorology");
    /** _more_          */
    public static final ExportType TYPE_RAW = new ExportType(4,
                                                  "Raw GPS data");
    /** _more_          */
    public static final ExportType CLASS_SITE_LOG = new ExportType(5,
                                                       "Site log");
    /** _more_          */
    public static final ExportType TYPE_SINEX = new ExportType(6,
                                                       "SINEX");
    /** _more_          */
    public static final ExportType TYPE_SP3 = new ExportType(7,
                                                       "sp3");



	public static ExportType findType(ExportType[] types, int id) {
		for (ExportType type : types) {
			if (id == type.getId())
				return type;
		}
		return null;
	}


	 /** _more_          */
    public static final ExportType[] GROUP_ALL_TYPES = new ExportType[] {
        TYPE_RINEX_OBS,
        TYPE_RINEX_NAV,
        TYPE_RINEX_MET,
        TYPE_RAW,
        CLASS_SITE_LOG,
        TYPE_SINEX,
        TYPE_SP3
    };




	/**
	 *  Set the Id property.
	 *
	 *  @param value The new value for Id
	 */
	public void setId(int value) {
		_id = value;
	}

	/**
	 *  Get the Id property.
	 *
	 *  @return The Id
	 */
	public int getId() {
		return _id;
	}

	/**
	 *  Set the Name property.
	 *
	 *  @param value The new value for Name
	 */
	public void setName(String value) {
		_name = value;
	}

	/**
	 *  Get the Name property.
	 *
	 *  @return The Name
	 */
	public String getName() {
		return _name;
	}

	int _id;

	String _name;
}
