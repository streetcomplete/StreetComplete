package de.westnordost.streetcomplete.data.meta

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.FeatureDictionary
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.source
import org.koin.dsl.module
import platform.Foundation.NSBundle

actual val metadataPlatformModule = module {
    val dir = NSBundle.mainBundle.resourcePath + "/compose-resources/files/"

    single<CountryBoundaries> {
        val file = Path(dir + "boundaries.ser")
        val source = SystemFileSystem.source(file).buffered()
        CountryBoundaries.deserializeFrom(source)
    }

    single<FeatureDictionary> {
        FeatureDictionary.create(
            fileSystem = SystemFileSystem,
            presetsBasePath = dir + "osmfeatures/default",
            brandPresetsBasePath = dir + "osmfeatures/brands"
        )
    }
}
