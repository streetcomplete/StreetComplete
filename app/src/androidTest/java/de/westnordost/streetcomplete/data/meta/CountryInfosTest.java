package de.westnordost.streetcomplete.data.meta;

import android.content.res.AssetManager;
import android.test.AndroidTestCase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.westnordost.streetcomplete.quests.opening_hours.Weekdays;

public class CountryInfosTest extends AndroidTestCase
{
	private void checkFirstDayOfWorkweekIsValid(CountryInfo info)
	{
		assertNotNull(info.getFirstDayOfWorkweek());
		assertTrue(Weekdays.getWeekdayIndex(info.getFirstDayOfWorkweek()) > -1);
	}

	private void checkSpeedUnitIsEitherKmhOrMph(CountryInfo info)
	{
		assertNotNull(info.getSpeedUnit());
		assertTrue(info.getSpeedUnit().equals("mph") || info.getSpeedUnit().equals("km/h"));
	}

	private void checkAdditionalValidHousenumberRegexes(Map<String, CountryInfo> infos)
	{
		assertTrue("99 bis".matches(infos.get("FR").getAdditionalValidHousenumberRegex()));
		assertTrue("ev.99".matches(infos.get("CZ").getAdditionalValidHousenumberRegex()));
		assertTrue("s/n".matches(infos.get("ES").getAdditionalValidHousenumberRegex()));
	}

	private void checkForEach(CountryInfo info)
	{
		checkFirstDayOfWorkweekIsValid(info);
		checkSpeedUnitIsEitherKmhOrMph(info);
	}

	public void testAll() throws IOException
	{
		Map<String, CountryInfo> infos = getAllCountryInfos();
		for(Map.Entry<String,CountryInfo> elem : infos.entrySet())
		{
			try
			{
				checkForEach(elem.getValue());

			}
			catch (Throwable e)
			{
				throw new RuntimeException("Error for "+ elem.getKey(), e);
			}
		}

		checkAdditionalValidHousenumberRegexes(infos);
	}

	private interface EachCountryInfo
	{
		void check(CountryInfo countryInfo);
	}

	private Map<String, CountryInfo> getAllCountryInfos() throws IOException
	{
		AssetManager am = getContext().getAssets();
		String[] fileList = am.list("countryInfos");
		CountryInfos cis = new CountryInfos(am);
		Map<String,CountryInfo> all = new HashMap<>();
		for(int i = 0; i < fileList.length; ++i)
		{
			String filename = fileList[i];
			String country = filename.substring(0, filename.lastIndexOf("."));
			try
			{
				CountryInfo info = cis.get(country);
				all.put(country,info);
			}
			catch (Throwable e)
			{
				throw new RuntimeException("Error for " + filename, e);
			}
		}
		return all;
	}
}
