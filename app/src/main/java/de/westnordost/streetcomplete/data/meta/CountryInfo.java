package de.westnordost.streetcomplete.data.meta;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class CountryInfo implements Serializable
{
	public static final long serialVersionUID = 1L;

	String speedUnit;
	List<String> popularSports;
	String firstDayOfWorkweek;
	Integer regularShoppingDays;
	String maxspeedLayout;
	String additionalValidHousenumberRegex;

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

	public String getAdditionalValidHousenumberRegex()
	{
		return additionalValidHousenumberRegex;
	}
}
