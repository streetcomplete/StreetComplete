package de.westnordost.streetcomplete.data.meta

import android.content.res.AssetManager
import dagger.Module
import dagger.Provides
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.AndroidFeatureDictionary
import de.westnordost.osmfeatures.FeatureDictionary
import java.util.concurrent.FutureTask
import javax.inject.Singleton

@Module
object MetadataModule {

	@Provides @Singleton fun countryInfos(assetManager: AssetManager, countryBoundaries: FutureTask<CountryBoundaries>): CountryInfos =
        CountryInfos(assetManager, countryBoundaries)

	@Provides @Singleton fun countryBoundariesFuture(assetManager: AssetManager): FutureTask<CountryBoundaries> =
        FutureTask { CountryBoundaries.load(assetManager.open("boundaries.ser")) }

    @Provides @Singleton fun featureDictionaryFuture(assetManager: AssetManager): FutureTask<FeatureDictionary> =
        FutureTask { AndroidFeatureDictionary.create(assetManager, "osmfeatures") }
}