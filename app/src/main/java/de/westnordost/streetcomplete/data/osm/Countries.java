package de.westnordost.streetcomplete.data.osm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class Countries
{
	public static final Countries ALL = new Countries(true, null);
	public static final Countries NONE = new Countries(false, null);

	public static Countries allExcept(String[] iso3166CountryCodes)
	{
		return new Countries(true, iso3166CountryCodes);
	}

	public static Countries noneExcept(String[] iso3166CountryCodes)
	{
		return new Countries(false, iso3166CountryCodes);
	}

	private final boolean defaultAll;
	private final String[] exceptions;

	private Countries(boolean defaultAllCountries, String[] exceptions)
	{
		this.defaultAll = defaultAllCountries;
		this.exceptions = exceptions;
	}

	public boolean isAllCountries()
	{
		return defaultAll && (exceptions == null || exceptions.length == 0);
	}

	/** @return true if it is all countries except the exceptions, false if it is no countries except the exceptions */
	public boolean isAllExcept()
	{
		return defaultAll;
	}

	public Collection<String> getExceptions()
	{
		return exceptions != null ? Arrays.asList(exceptions) : Collections.emptyList();
	}
}
