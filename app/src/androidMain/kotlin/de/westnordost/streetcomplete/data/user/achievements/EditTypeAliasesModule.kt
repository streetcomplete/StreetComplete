package de.westnordost.streetcomplete.data.user.achievements

import org.koin.core.qualifier.named
import org.koin.dsl.module

import de.westnordost.streetcomplete.overlays.places.PlacesOverlay
import de.westnordost.streetcomplete.overlays.street_parking.StreetParkingOverlay
import de.westnordost.streetcomplete.quests.amenity_cover.AddAmenityCover
import de.westnordost.streetcomplete.quests.amenity_indoor.AddIsAmenityIndoor
import de.westnordost.streetcomplete.quests.building_type.AddBuildingType
import de.westnordost.streetcomplete.quests.crossing_markings.AddCrossingMarkings
import de.westnordost.streetcomplete.quests.cycleway.AddCycleway
import de.westnordost.streetcomplete.quests.foot.AddProhibitedForPedestrians
import de.westnordost.streetcomplete.quests.moped.AddMopedAccess
import de.westnordost.streetcomplete.quests.oneway.AddOneway
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface
import de.westnordost.streetcomplete.quests.traffic_signals_vibrate.AddTrafficSignalsVibration
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessPublicTransport
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessToilets

val editTypeAliasesModule = module {
    factory(named("TypeAliases")) { typeAliases }
}

// list of (quest) synonyms (this alternate name is mentioned to aid searching for this code)
private val typeAliases = listOf(
    "AddAccessibleForPedestrians"        to AddProhibitedForPedestrians::class.simpleName!!,
    "AddWheelChairAccessPublicTransport" to AddWheelchairAccessPublicTransport::class.simpleName!!,
    "AddWheelChairAccessToilets"         to AddWheelchairAccessToilets::class.simpleName!!,
    "AddSidewalks"                       to AddSidewalk::class.simpleName!!,
    "DetailRoadSurface"                  to AddRoadSurface::class.simpleName!!,
    "AddTrafficSignalsBlindFeatures"     to AddTrafficSignalsVibration::class.simpleName!!,
    "AddSuspectedOneway"                 to AddOneway::class.simpleName!!,
    "AddPicnicTableCover"                to AddAmenityCover::class.simpleName!!,
    "AddCrossingType"                    to AddCrossingMarkings::class.simpleName!!,
    // whether lit roads have been added in context of the quest or the overlay should not matter for the statistics
    "WayLitOverlay"                      to AddWayLit::class.simpleName!!,
    "SidewalkOverlay"                    to AddSidewalk::class.simpleName!!,
    "CyclewayOverlay"                    to AddCycleway::class.simpleName!!,
    "BuildingsOverlay"                   to AddBuildingType::class.simpleName!!,
    "AddStreetParking"                   to StreetParkingOverlay::class.simpleName!!,
    "AddIsDefibrillatorIndoor"           to AddIsAmenityIndoor::class.simpleName!!,
    "ShopsOverlay"                       to PlacesOverlay::class.simpleName!!,
    "AddProhibitedForMoped"              to AddMopedAccess::class.simpleName!!
)
