package de.westnordost.streetcomplete.data.map

import org.koin.dsl.module

val mapModule = module {
    factory { MapStateStore(get()) }
}
