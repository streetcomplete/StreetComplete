package de.westnordost.streetcomplete.data.meta;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CountryInfo implements Serializable, Cloneable
{
	public static final long serialVersionUID = 1L;

	// this value is not defined in the yaml file but it is the file name!
	String countryCode;

	String speedUnit;
	List<String> popularSports;
	String firstDayOfWorkweek;
	Integer regularShoppingDays;
	String maxspeedLayout;
	String additionalValidHousenumberRegex;
	List<String> officialLanguages;
	Boolean isSlowZoneKnown;

	public String getSpeedUnit()
	{
		return speedUnit;
	}

	public List<String> getPopularSports()
	{
		return Collections.unmodifiableList(popularSports);
	}

	public String getFirstDayOfWorkweek()
	{
		return firstDayOfWorkweek;
	}

	public Integer getRegularShoppingDays()
	{
		return regularShoppingDays;
	}

	public String getMaxspeedLayout()
	{
		return maxspeedLayout;
	}

	public boolean isSlowZoneKnown()
	{
		return isSlowZoneKnown;
	}

	public String getAdditionalValidHousenumberRegex()
	{
		return additionalValidHousenumberRegex;
	}

	public List<String> getOfficialLanguages()
	{
		return Collections.unmodifiableList(officialLanguages);
	}

	public String getCountryCode()
	{
		return countryCode;
	}

	public Locale getLocale()
	{
		List<String> languages = getOfficialLanguages();
		if (languages != null && languages.size() > 0)
		{
			return new Locale(languages.get(0), countryCode);
		}
		return Locale.getDefault();
	}
}
