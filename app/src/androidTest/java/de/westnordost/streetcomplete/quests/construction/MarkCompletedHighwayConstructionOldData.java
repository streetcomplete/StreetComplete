package de.westnordost.streetcomplete.quests.construction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.DateUtil;

public class MarkCompletedHighwayConstructionOldData extends MarkCompletedHighwayConstruction {
	private Date date;
	MarkCompletedHighwayConstructionOldData(OverpassMapDataDao overpassServer, String dateString) throws ParseException {
		super(overpassServer);
		date = DateUtil.basicISO8601().parse(dateString);
	}

	@Override
	protected String getCurrentDateString(){
		return DateUtil.getOffsetDateStringFromDate(0, date) + "T00:00:00Z";
	}

	@Override
	protected String getOffsetDateString(int offset){
		return DateUtil.getOffsetDateStringFromDate(offset, date) + "T00:00:00Z";
	}
}
