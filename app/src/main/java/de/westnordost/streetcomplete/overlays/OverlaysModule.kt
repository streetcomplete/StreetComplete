package de.westnordost.streetcomplete.overlays

import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.overlays.address.AddressOverlay
import de.westnordost.streetcomplete.overlays.shops.ShopsOverlay
import de.westnordost.streetcomplete.overlays.sidewalk.SidewalkOverlay
import de.westnordost.streetcomplete.overlays.street_parking.StreetParkingOverlay
import de.westnordost.streetcomplete.overlays.way_lit.WayLitOverlay
import org.koin.core.qualifier.named
import org.koin.dsl.module

val overlaysModule = module {
    single { OverlayRegistry(listOf(
        WayLitOverlay(),
        SidewalkOverlay(),
        StreetParkingOverlay(),
        AddressOverlay(),
        ShopsOverlay(get(named("FeatureDictionaryFuture"))),
    )) }
}
