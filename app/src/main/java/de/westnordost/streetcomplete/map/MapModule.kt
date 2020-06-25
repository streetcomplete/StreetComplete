package de.westnordost.streetcomplete.map

import dagger.Module
import dagger.Provides

@Module object MapModule {

    @Provides fun jawg(): VectorTileProvider = VectorTileProvider(
        "JawgMaps",
        "© JawgMaps",
        "https://www.jawg.io",
        "https://www.jawg.io/en/confidentiality/",
        "map_theme/jawg",
        String(byteArrayOf(
            109,76,57,88,52,83,119,120,102,115,65,71,102,111,106,118,71,105,105,111,110,57,104,80,
            75,117,71,76,75,120,80,98,111,103,76,121,77,98,116,97,107,65,50,103,74,51,88,56,56,103,
            99,86,108,84,83,81,55,79,68,54,79,102,98,90
        ))
    )
}