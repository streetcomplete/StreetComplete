package de.westnordost.streetcomplete.data.osm.geometry

import org.koin.dsl.module

val elementGeometryModule = module {
    factory { ElementGeometryCreator() }
    factory { ElementGeometryDao(get(), get(), get()) }
    factory { PolylinesSerializer() }
}
