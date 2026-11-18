package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.building.BuildingType.*
import de.westnordost.streetcomplete.osm.building.BuildingLifecycle.*
import de.westnordost.streetcomplete.ui.common.item_select.Group
import kotlinx.serialization.Serializable

@Serializable
enum class BuildingTypeCategory {
    RESIDENTIAL, COMMERCIAL, CIVIC, OUTBUILDING, RELIGIOUS, FOR_FARMS, OTHER, STATUS;

    companion object {
        val buildingGroups: List<Group<BuildingTypeCategory, BuildingType>> = listOf(
            Group(RESIDENTIAL, BuildingType.RESIDENTIAL, listOf(
                DETACHED, APARTMENTS, SEMI_DETACHED, TERRACE, TERRACED_HOUSE, HOUSE, FARM, HUT,
                BUNGALOW, STATIC_CARAVAN, DORMITORY, HOUSEBOAT,
            )),
            Group(COMMERCIAL, BuildingType.COMMERCIAL, listOf(
                OFFICE, RETAIL, KIOSK, INDUSTRIAL, WAREHOUSE, HOTEL, STORAGE_TANK, PARKING, HANGAR,
                BUNGALOW,
            )),
            Group(CIVIC, BuildingType.CIVIC, listOf(
                SCHOOL, UNIVERSITY, HOSPITAL, KINDERGARTEN, SPORTS_CENTRE, TRAIN_STATION,
                TRANSPORTATION, COLLEGE, GOVERNMENT, TOILETS, STADIUM, FIRE_STATION, OFFICE,
                GRANDSTAND
            )),
            Group(OUTBUILDING, BuildingType.OUTBUILDING, listOf(
                SHED, GARAGE, GARAGES, CARPORT, SERVICE, ALLOTMENT_HOUSE, GREENHOUSE, ROOF,
                BOATHOUSE, CONTAINER, TENT, GUARDHOUSE,
            )),
            Group(RELIGIOUS, BuildingType.RELIGIOUS, listOf(
                CHURCH, CATHEDRAL, CHAPEL, MOSQUE, TEMPLE, PAGODA, SYNAGOGUE, SHRINE
            )),
            Group(FOR_FARMS, null, listOf(
                FARM, FARM_AUXILIARY, SILO, GREENHOUSE, STORAGE_TANK, SHED,
            )),
            Group(OTHER, null, listOf(
                ROOF, TOWER, BUNKER, BRIDGE, TOMB,
            )),
        )

        val statusGroups: List<Group<BuildingTypeCategory, BuildingStatus>> =
            buildingGroups.map { Group<BuildingTypeCategory, BuildingStatus>(it.category, it.item, it.children) } +
            Group(STATUS, null, listOf<BuildingStatus>(CONSTRUCTION, DISUSED, ABANDONED, RUINS))
    }
}
