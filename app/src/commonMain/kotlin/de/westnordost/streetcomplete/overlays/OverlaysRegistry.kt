package de.westnordost.streetcomplete.overlays

import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.overlays.address.AddressOverlay
import de.westnordost.streetcomplete.overlays.buildings.BuildingsOverlay
import de.westnordost.streetcomplete.overlays.cycleway.CyclewayOverlay
import de.westnordost.streetcomplete.overlays.mtb_scale.MtbScaleOverlay
import de.westnordost.streetcomplete.overlays.places.PlacesOverlay
import de.westnordost.streetcomplete.overlays.sidewalk.SidewalkOverlay
import de.westnordost.streetcomplete.overlays.street_parking.StreetParkingOverlay
import de.westnordost.streetcomplete.overlays.surface.SurfaceOverlay
import de.westnordost.streetcomplete.overlays.things.ThingsOverlay
import de.westnordost.streetcomplete.overlays.way_lit.WayLitOverlay

fun overlaysRegistry(
    getCountryInfoByLocation: (LatLon) -> CountryInfo,
    getCountryCodeByLocation: (LatLon) -> String?,
    getFeature: (Element) -> Feature?,
) = OverlayRegistry(listOf(

    /* Each overlay is assigned an ordinal. This is used for serialization and is thus never
     * changed, even if the order of overlays is changed.  */
    0 to WayLitOverlay(),
    6 to SurfaceOverlay(),
    1 to SidewalkOverlay(),
    5 to CyclewayOverlay(getCountryInfoByLocation),
    2 to StreetParkingOverlay(),
    3 to AddressOverlay(getCountryCodeByLocation),
    4 to PlacesOverlay(getFeature),
    8 to ThingsOverlay(getFeature),
    7 to BuildingsOverlay(),
    9 to MtbScaleOverlay(),
))
