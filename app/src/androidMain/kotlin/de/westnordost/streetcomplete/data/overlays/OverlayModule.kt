package de.westnordost.streetcomplete.data.overlays

import org.koin.dsl.module

val overlayModule = module {
    single<SelectedOverlaySource> { get<SelectedOverlayController>() }
    single { SelectedOverlayController(get(), get()) }
}
