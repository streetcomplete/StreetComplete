package de.westnordost.streetcomplete.data.meta;

import android.content.res.AssetManager;

import com.vividsolutions.jts.geom.GeometryCollection;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.streetcomplete.util.StreamUtils;

@Module
public class MetadataModule
{
	@Provides @Singleton public static CountryInfos countryInfos(
			AssetManager assetManager, FutureTask<CountryBoundaries> countryBoundaries)
	{
		return new CountryInfos(assetManager, countryBoundaries);
	}

	@Provides public static GeoJsonReader geoJsonReader()
	{
		return new GeoJsonReader();
	}

	@Provides @Singleton public static FutureTask<CountryBoundaries> countryBoundariesFuture(
			final AssetManager assetManager, final GeoJsonReader geoJsonReader)
	{
		return new FutureTask<>(new Callable<CountryBoundaries>()
		{
			@Override public CountryBoundaries call() throws Exception
			{
				InputStream is = assetManager.open("countryBoundaries.json");
				return new CountryBoundaries(
						(GeometryCollection) geoJsonReader.read(StreamUtils.readToString(is)));
			}
		});
	}
}
