package de.westnordost.streetcomplete.data.meta;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;

import java.util.concurrent.FutureTask;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.countryboundaries.CountryBoundaries;
import de.westnordost.osmfeatures.AndroidFeatureDictionary;
import de.westnordost.osmfeatures.FeatureDictionary;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.map.TangramQuestSpriteSheet;

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

	@Provides @Singleton public static FutureTask<FeatureDictionary> featureDictionaryFuture(AssetManager assetManager)
	{
		return new FutureTask<>(() -> AndroidFeatureDictionary.create(assetManager, "osmfeatures"));
	}

	@Provides @Singleton public static TangramQuestSpriteSheet tangramQuestSpriteSheetCreator(
		Context context, QuestTypeRegistry questTypeRegistry, SharedPreferences prefs
	)
	{
		return new TangramQuestSpriteSheet(context, questTypeRegistry, prefs);
	}
}
