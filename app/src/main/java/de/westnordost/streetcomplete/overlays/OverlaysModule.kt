package de.westnordost.streetcomplete.overlays

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.overlays.address.AddressOverlay
import de.westnordost.streetcomplete.overlays.cycleway.CyclewayOverlay
import de.westnordost.streetcomplete.overlays.shops.ShopsOverlay
import de.westnordost.streetcomplete.overlays.sidewalk.SidewalkOverlay
import de.westnordost.streetcomplete.overlays.street_parking.StreetParkingOverlay
import de.westnordost.streetcomplete.overlays.surface.SurfaceOverlay
import de.westnordost.streetcomplete.overlays.way_lit.WayLitOverlay
import de.westnordost.streetcomplete.util.ktx.getFeature
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.FutureTask

/* Each overlay is assigned an ordinal. This is used for serialization and is thus never changed,
*  even if the order of overlays is changed.  */
val overlaysModule = module {
    single {
        overlaysRegistry(
            get(),
            get(named("CountryBoundariesFuture")),
            { tags ->
                get<FutureTask<FeatureDictionary>>(named("FeatureDictionaryFuture"))
                .get().getFeature(tags)
            }
        )
    }
}

fun overlaysRegistry(
    countryInfos: CountryInfos,
    countryBoundariesFuture: FutureTask<CountryBoundaries>,
    getFeature: (tags: Map<String, String>) -> Feature?,
) = OverlayRegistry(listOf(

    0 to WayLitOverlay(),
    6 to SurfaceOverlay(),
    1 to SidewalkOverlay(),
    5 to CyclewayOverlay(countryInfos, countryBoundariesFuture),
    2 to StreetParkingOverlay(),
    3 to AddressOverlay(countryBoundariesFuture),
    4 to ShopsOverlay(getFeature),
))
