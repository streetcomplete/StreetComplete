package de.westnordost.streetcomplete.data.presets

import org.koin.dsl.module

val editTypePresetsModule = module {
    factory { EditTypePresetsDao(get()) }

    single<EditTypePresetsSource> { get<EditTypePresetsController>() }
    single { EditTypePresetsController(get(), get()) }
}
