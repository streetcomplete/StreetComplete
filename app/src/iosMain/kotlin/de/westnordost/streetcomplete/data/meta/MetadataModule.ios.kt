package de.westnordost.streetcomplete.data.meta

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

    single<de.westnordost.countryboundaries.CountryBoundaries> {
        val file = Path(dir + "boundaries.ser")
        val source = SystemFileSystem.source(file).buffered()
        de.westnordost.countryboundaries.CountryBoundaries.deserializeFrom(source)
    }

    single<FeatureDictionary> {
        FeatureDictionary.create(
            fileSystem = SystemFileSystem,
            presetsBasePath = dir + "osmfeatures/default",
            brandPresetsBasePath = dir + "osmfeatures/brands"
        )
    }
}
