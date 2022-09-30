package de.westnordost.streetcomplete.overlays

import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.overlays.custom.CustomOverlay
import de.westnordost.streetcomplete.overlays.address.AddressOverlay
import de.westnordost.streetcomplete.overlays.sidewalk.SidewalkOverlay
import de.westnordost.streetcomplete.overlays.street_parking.StreetParkingOverlay
import de.westnordost.streetcomplete.overlays.way_lit.WayLitOverlay
import org.koin.dsl.module

val overlaysModule = module {
    single { OverlayRegistry(listOf(
        WayLitOverlay(),
        SidewalkOverlay(),
        StreetParkingOverlay(),
        AddressOverlay(),
        CustomOverlay(get()),
    )) }
}
