package de.westnordost.streetcomplete.data.meta

import android.content.res.AssetManager
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.create
import kotlinx.io.asSource
import kotlinx.io.buffered
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val metadataPlatformModule = module {
    val dir = "composeResources/de.westnordost.streetcomplete.resources/files/"

    single<de.westnordost.countryboundaries.CountryBoundaries> {
        val source = get<AssetManager>().open(dir + "boundaries.ser").asSource().buffered()
        de.westnordost.countryboundaries.CountryBoundaries.deserializeFrom(source)
    }

    single<FeatureDictionary> {
        FeatureDictionary.create(
            assetManager = get<AssetManager>(),
            presetsBasePath = dir + "osmfeatures/default",
            brandPresetsBasePath = dir + "osmfeatures/brands"
        )
    }
}
