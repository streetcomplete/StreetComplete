package de.westnordost.streetcomplete.quests;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {
	public static SimpleDateFormat basicISO8601(){
		return new SimpleDateFormat("yyyy-MM-dd");

	}

	public static String getOffsetDateStringFromDate(int offsetInDays, Calendar cal){
		cal.add(Calendar.DAY_OF_MONTH, offsetInDays);
		return basicISO8601().format(cal.getTime());
	}

	public static String getOffsetDateString(int offsetInDays){
		return getOffsetDateStringFromDate(offsetInDays, Calendar.getInstance());
	}

	public static String getCurrentDateString() {
		return getOffsetDateString(0);
	}
}
