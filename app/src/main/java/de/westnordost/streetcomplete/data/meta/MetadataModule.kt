package de.westnordost.streetcomplete.data.meta

import android.content.res.AssetManager
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.AndroidFeatureDictionary
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.FutureTask

val metadataModule = module {
    single { AbbreviationsByLocale(get()) }
    single { CountryInfos(get()) }
    single(named("CountryBoundariesFuture")) { FutureTask { CountryBoundaries.load(get<AssetManager>().open("boundaries.ser")) } }
    single(named("FeatureDictionaryFuture")) { FutureTask { AndroidFeatureDictionary.create(get(), "osmfeatures/default", "osmfeatures/brands") } }
}
