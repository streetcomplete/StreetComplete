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

	private void checkAll(CountryInfo info)
	{
		checkFirstDayOfWorkweekIsValid(info);
	}

	public void testAll() throws IOException
	{
		for(Map.Entry<String,CountryInfo> elem : getAllCountryInfos().entrySet())
		{
			try
			{
				checkAll(elem.getValue());

			}
			catch (Throwable e)
			{
				throw new RuntimeException("Error for "+ elem.getKey(), e);
			}
		}
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
