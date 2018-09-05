package de.westnordost.streetcomplete.data.meta;

import android.content.res.AssetManager;

import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.inject.Inject;

import de.westnordost.countryboundaries.CountryBoundaries;

public class CountryInfos
{
	private static final String BASEPATH = "country_metadata";

	private final AssetManager assetManager;
	private final Future<CountryBoundaries> countryBoundaries;
	private final Map<String, CountryInfo> countryInfoMap;

	private CountryInfo defaultCountryInfo;


	@Inject public CountryInfos(AssetManager assetManager, Future<CountryBoundaries> countryBoundaries)
	{
		this.assetManager = assetManager;
		this.countryBoundaries = countryBoundaries;
		countryInfoMap = new HashMap<>();
	}

	/** Get the info by location */
	public CountryInfo get(double longitude, double latitude)
	{
		try
		{
			List<String> countryCodesIso3166 = countryBoundaries.get().getIds(longitude, latitude);
			return get(countryCodesIso3166);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/** Get the info by a list of country codes sorted by size. I.e. DE-NI,DE,EU gets the info
	 *  for Niedersachsen in Germany and uses defaults from Germany and from the European Union */
	public CountryInfo get(List<String> countryCodesIso3166)
	{
		CountryInfo result = new CountryInfo();
		for(String isoCode : countryCodesIso3166)
		{
			CountryInfo countryInfo = get(isoCode);
			if(countryInfo != null)
			{
				complement(result, countryInfo);
			}
		}
		complement(result,getDefault());
		return result;
	}

	private CountryInfo get(String countryCodeIso3166)
	{
		if(!countryInfoMap.containsKey(countryCodeIso3166))
		{
			CountryInfo info = load(countryCodeIso3166);
			countryInfoMap.put(countryCodeIso3166, info);
		}
		return countryInfoMap.get(countryCodeIso3166);
	}

	private CountryInfo load(String countryCodeIso3166)
	{
		try
		{
			List<String> countryInfosFiles = Arrays.asList(assetManager.list(BASEPATH));

			if(countryInfosFiles.contains(countryCodeIso3166+".yml"))
			{
				return loadCountryInfo(countryCodeIso3166);
			}
			return null;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private CountryInfo getDefault()
	{
		try
		{
			if (defaultCountryInfo == null) defaultCountryInfo = loadCountryInfo("default");
			return defaultCountryInfo;
		}
		catch (Exception e)
		{
			// this should be in any case a programming error
			throw new RuntimeException(e);
		}
	}

	private CountryInfo loadCountryInfo(String countryCodeIso3166) throws IOException
	{
		String filename = countryCodeIso3166+".yml";
		InputStream is = null;
		try
		{
			is = assetManager.open(BASEPATH + File.separator + filename);
			Reader reader =  new InputStreamReader(is, "UTF-8");
			YamlReader yamlReader = new YamlReader(reader);
			yamlReader.getConfig().setPrivateFields(true);
			CountryInfo result = yamlReader.read(CountryInfo.class);
			result.countryCode = countryCodeIso3166.split("-")[0];
			return result;
		}
		finally
		{
			if(is != null) try
			{
				is.close();
			}
			catch (IOException ignore) { }
		}
	}

	/** Complement every declared field that is null in ´info´ with the field in ´with´ */
	private void complement(CountryInfo info, CountryInfo with)
	{
		try
		{
			for(Field field : info.getClass().getDeclaredFields())
			{
				if(field.get(info) == null)
				{
					Field complementingField = with.getClass().getDeclaredField(field.getName());
					field.set(info, complementingField.get(with));
				}
			}
		}
		catch (Exception e) // IllegalAccessException, NoSuchFieldException
		{
			// this should be in any case a programming error
			throw new RuntimeException(e);
		}
	}
}
