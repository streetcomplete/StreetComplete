package de.westnordost.streetcomplete.quests;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
	public static SimpleDateFormat basicISO8601(){
		return new SimpleDateFormat("yyyy-MM-dd", Locale.US);

	}
	public static String getOffsetDateStringFromDate(final int offsetInDays, final Date date){
		Calendar modifiedCalendar = Calendar.getInstance();
		modifiedCalendar.setTime(date);
		modifiedCalendar.add(Calendar.DAY_OF_MONTH, offsetInDays);
		return basicISO8601().format(modifiedCalendar.getTime());
	}

	public static String getOffsetDateString(int offsetInDays){
		return getOffsetDateStringFromDate(offsetInDays, Calendar.getInstance().getTime());
	}

	public static String getCurrentDateString() {
		return getOffsetDateString(0);
	}
}
