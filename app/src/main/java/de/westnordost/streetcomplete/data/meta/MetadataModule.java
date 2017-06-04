package de.westnordost.streetcomplete.data.meta;

import android.content.res.AssetManager;

import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MetadataModule
{
	@Provides @Singleton public static CountryInfos countryInfos(
			AssetManager assetManager, CountryBoundaries countryBoundaries)
	{
		return new CountryInfos(assetManager, countryBoundaries);
	}

	@Provides @Singleton public static CountryBoundaries countryBoundaries(AssetManager assetManager)
	{
		try
		{
			return new CountryBoundaries(assetManager.open("countryBoundaries.json"));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
