package de.westnordost.streetcomplete.data.meta

import android.content.res.AssetManager
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.create
import kotlinx.io.asSource
import kotlinx.io.buffered
import org.koin.core.qualifier.named
import org.koin.dsl.module

val metadataModule = module {
    single { AbbreviationsByLocale(get()) }
    single { CountryInfos(get()) }
    single<Lazy<CountryBoundaries>>(named("CountryBoundariesLazy")) {
        lazy {
            val source = get<AssetManager>().open("boundaries.ser").asSource().buffered()
            CountryBoundaries.deserializeFrom(source)
        }
    }
    single<Lazy<FeatureDictionary>>(named("FeatureDictionaryLazy")) {
        lazy {
            FeatureDictionary.create(get<AssetManager>(), "osmfeatures/default", "osmfeatures/brands")
        }
    }
}
