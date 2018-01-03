package de.westnordost.streetcomplete.data.osm;

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

	public String[] getDisabledCountries()
	{
		return defaultAll ? exceptions : null;
	}

	public String[] getEnabledCountries()
	{
		return defaultAll ? null : exceptions;
	}
}
