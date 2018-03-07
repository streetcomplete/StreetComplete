package de.westnordost.streetcomplete.quests.add_housenumber;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumber;


public class AddHousenumberOldData extends AddHousenumber {
	private String date;
	AddHousenumberOldData(OverpassMapDataDao overpassServer, String date) {
		super(overpassServer);
		this.date = date;
	}

	private String atticDataRequest(){
		return "[date:\"" + date + "\"]";
	}

	protected String getBuildingsWithoutAddressesOverpassQuery(BoundingBox bbox){
		return atticDataRequest() + ";" + super.getBuildingsWithoutAddressesOverpassQuery(bbox);
	}

	protected String getFreeFloatingAddressesOverpassQuery(BoundingBox bbox){
		return atticDataRequest() + super.getFreeFloatingAddressesOverpassQuery(bbox);
	}

	protected String getNonBuildingAreasWithAddressesQuery(BoundingBox bbox){
		return atticDataRequest() + super.getNonBuildingAreasWithAddressesQuery(bbox);
	}


}
