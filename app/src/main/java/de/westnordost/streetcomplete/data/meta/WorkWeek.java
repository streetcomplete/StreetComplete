package de.westnordost.streetcomplete.data.meta;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Source: https://en.wikipedia.org/wiki/Workweek_and_weekend#Around_the_world (retrieved 27.10.2016)
*/
public class WorkWeek
{
	private static final List<String> startSundayCountries = Arrays.asList(
			"AE", "BD", "BH", "DZ", "EG", "IL", "IQ", "JO", "KW", "LY", "MA", "MV", "OM", "PS",
			"QA", "SA", "SD", "SY", "YE", "NP");

	private static final List<String> startSaturdayCountries = Arrays.asList("AF", "DJ","IR");

	/** @return the first day in a workweek (not necessarily the first day of the week in a
	 *  calendar) */
	public static int getFirstDay(Locale locale)
	{
		if(startSundayCountries.contains(locale.getCountry()))
			return Calendar.SUNDAY;
		if(startSaturdayCountries.contains(locale.getCountry()))
			return Calendar.SATURDAY;
		return Calendar.MONDAY;
	}

	/*	according to https://en.wikipedia.org/wiki/Shopping_hours the norm for shopping days
		is rather MO-SA (=6 days), pretty much only in Germany / Austria, shops often have shorter
		opening hours on SA. If you are reading this and disagree, feel free to extend the
		above article, it is not too complete IMO.
		This here is not about the https://en.wikipedia.org/wiki/Workweek_and_weekend though,
		shops seem to be regularly open on the weekend as well in many countries.
	*/
	public static int getRegularShoppingDays(Locale locale)
	{
		switch(locale.getCountry())
		{
			case "DE":
			case "AT":
				return 5;
			default:
				return 6;
		}
	}
}
