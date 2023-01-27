package de.westnordost.streetcomplete.overlays

import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.overlays.address.AddressOverlay
import de.westnordost.streetcomplete.overlays.cycleway.CyclewayOverlay
import de.westnordost.streetcomplete.overlays.shops.ShopsOverlay
import de.westnordost.streetcomplete.overlays.sidewalk.SidewalkOverlay
import de.westnordost.streetcomplete.overlays.street_parking.StreetParkingOverlay
import de.westnordost.streetcomplete.overlays.way_lit.WayLitOverlay
import org.koin.core.qualifier.named
import org.koin.dsl.module

/* Each overlay is assigned an ordinal. This is used for serialization and is thus never changed,
*  even if the order of overlays is changed.  */
val overlaysModule = module {
    single { OverlayRegistry(listOf(
        0 to WayLitOverlay(),
        1 to SidewalkOverlay(),
        2 to StreetParkingOverlay(),
        3 to AddressOverlay(),
        4 to ShopsOverlay(get(named("FeatureDictionaryFuture"))),
        5 to CyclewayOverlay(get(), get(named("CountryBoundariesFuture"))),
    )) }
}
