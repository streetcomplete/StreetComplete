package de.westnordost.streetcomplete.data.meta;

import android.content.res.AssetManager;

import com.esotericsoftware.yamlbeans.YamlException;
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

public class CountryInfos
{
	private static final String BASEPATH = "countryInfos";

	private AssetManager assetManager;

	private CountryInfo defaultCountryInfo;

	private Map<String, CountryInfo> countryInfoMap;

	public CountryInfos(AssetManager assetManager)
	{
		this.assetManager = assetManager;
		countryInfoMap = new HashMap<>();
	}

	public CountryInfo get(String countryCodeIso3166)
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

			CountryInfo aDefault = getDefault();

			String filename = countryCodeIso3166+".yml";
			if(countryInfosFiles.contains(filename))
			{
				CountryInfo countryInfo = loadCountryInfo(filename);
				complement(countryInfo, aDefault);
				return countryInfo;
			}
			return aDefault;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
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

	private CountryInfo getDefault() throws IOException
	{
		if(defaultCountryInfo == null) defaultCountryInfo = loadCountryInfo("default.yml");
		return defaultCountryInfo;
	}

	private CountryInfo loadCountryInfo(String filename) throws IOException
	{
		InputStream is = null;
		try
		{
			is = assetManager.open(BASEPATH + File.separator + filename);
			Reader reader =  new InputStreamReader(is, "UTF-8");
			YamlReader yamlReader = new YamlReader(reader);
			yamlReader.getConfig().setPrivateFields(true);
			return yamlReader.read(CountryInfo.class);
		}
		finally
		{
			if(is != null) try
			{
				is.close();
			}
			catch (IOException e) { }
		}
	}
}
