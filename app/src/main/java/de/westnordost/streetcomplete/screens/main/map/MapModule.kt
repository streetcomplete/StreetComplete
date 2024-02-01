package de.westnordost.streetcomplete.screens.main.map

import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.iconResId
import de.westnordost.streetcomplete.view.presetIconIndex
import org.koin.dsl.module

val mapModule = module {
    factory<VectorTileProvider> {
        object : VectorTileProvider(
            "JawgMaps",
            16,
            "© JawgMaps",
            "https://www.jawg.io",
            "https://www.jawg.io/en/confidentiality/",
            "map_theme/jawg",
            String(byteArrayOf(
                109, 76, 57, 88, 52, 83, 119, 120, 102, 115, 65, 71, 102, 111, 106, 118, 71, 105, 105, 111, 110, 57, 104, 80,
                75, 117, 71, 76, 75, 120, 80, 98, 111, 103, 76, 121, 77, 98, 116, 97, 107, 65, 50, 103, 74, 51, 88, 56, 56, 103,
                99, 86, 108, 84, 83, 81, 55, 79, 68, 54, 79, 102, 98, 90
            ))
        ) {
            override fun getTileUrl(zoom: Int, x: Int, y: Int): String =
                "https://tile.jawg.io/streets-v2/$zoom/$x/$y.pbf?access-token=$apiKey"
        }
    }

    single { TangramPinsSpriteSheet(get(), get(), get(), get()) }
    single {
        TangramIconsSpriteSheet(
            get(),
            get(),
            presetIconIndex.values + BuildingType.values().mapNotNull { it.iconResId }
        )
    }
}
