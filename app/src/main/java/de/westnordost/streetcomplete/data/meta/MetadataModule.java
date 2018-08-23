package de.westnordost.streetcomplete.data.meta;

import android.content.res.AssetManager;

import java.util.concurrent.FutureTask;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.countryboundaries.CountryBoundaries;

@Module
public class MetadataModule
{
	@Provides @Singleton public static CountryInfos countryInfos(
			AssetManager assetManager, FutureTask<CountryBoundaries> countryBoundaries)
	{
		return new CountryInfos(assetManager, countryBoundaries);
	}

	@Provides @Singleton public static FutureTask<CountryBoundaries> countryBoundariesFuture(
			final AssetManager assetManager)
	{
		return new FutureTask<>(() -> CountryBoundaries.load(assetManager.open("boundaries.ser")));
	}
}
