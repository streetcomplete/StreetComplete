package de.westnordost.streetcomplete.data.osm.download;

import javax.inject.Provider;

import de.westnordost.osmapi.OsmConnection;


public class OverpassOldMapDataDao extends OverpassMapDataDao {
	private final String date;

	public OverpassOldMapDataDao(OsmConnection osm, Provider<OverpassMapDataParser> parserProvider, String date) {
		super(osm, parserProvider);
		this.date = date;
	}

	private String atticDataRequest(){
		return "[date:\"" + date + "T00:00:00Z\"]";
	}

	private boolean isQueryWithoutSettings(String query){
		// heurestic, but quite reliable one
		// non-heurestic would require query parsing what would be unreasonable here
		return query.toCharArray()[0] != '[';
	}

	public synchronized void get(final String baseQuery, MapDataWithGeometryHandler handler) {
		String queryForOldData = atticDataRequest();
		if(isQueryWithoutSettings(baseQuery)){
			queryForOldData += ";";
		}
		queryForOldData += baseQuery;
		super.get(queryForOldData, handler);
	}
}
