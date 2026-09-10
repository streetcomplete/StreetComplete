package de.westnordost.streetcomplete

import de.westnordost.streetcomplete.screens.main.MainActivity
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.maplibre.compose.location.AndroidLocationProvider
import org.maplibre.compose.location.LocationProvider

val androidAppModule = module {
    scope<MainActivity> {
        scoped<LocationProvider> { AndroidLocationProvider(get<MainActivity>()) } onClose { it?.close() }
    }
}
