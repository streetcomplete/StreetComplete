package de.westnordost.streetcomplete.quests;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateHandler {
	public static String getOffsetDateString(int offsetInDays){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat basicISO8601 = new SimpleDateFormat("yyyy-MM-dd");
		cal.add(Calendar.DAY_OF_MONTH, offsetInDays);
		return basicISO8601.format(cal.getTime());
	}

	public static String getCurrentDateString() {
		return getOffsetDateString(0);
	}
}
