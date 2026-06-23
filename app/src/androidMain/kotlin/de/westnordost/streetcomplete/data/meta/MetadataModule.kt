package de.westnordost.streetcomplete.data.meta

import android.content.res.AssetManager
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.create
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundariesImpl
import kotlinx.io.asSource
import kotlinx.io.buffered
import org.koin.core.qualifier.named
import org.koin.dsl.module

val metadataModule = module {
    val dir = "composeResources/de.westnordost.streetcomplete.resources/files/"

    single { NameSuggestionsSource(get()) }
    single { CountryInfos(get()) }

    single<de.westnordost.countryboundaries.CountryBoundaries> {
        val source = get<AssetManager>().open(dir + "boundaries.ser").asSource().buffered()
        de.westnordost.countryboundaries.CountryBoundaries.deserializeFrom(source)
    }
    single<CountryBoundaries> { CountryBoundariesImpl(get()) }
    single<Lazy<CountryBoundaries>>(named("CountryBoundariesLazy")) {
        lazy { get() }
    }

    single<Lazy<FeatureDictionary>>(named("FeatureDictionaryLazy")) {
        lazy {
            FeatureDictionary.create(
                assetManager = get<AssetManager>(),
                presetsBasePath = dir + "osmfeatures/default",
                brandPresetsBasePath = dir + "osmfeatures/brands"
            )
        }
    }
}
