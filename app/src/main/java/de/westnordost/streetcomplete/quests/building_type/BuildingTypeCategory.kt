package de.westnordost.streetcomplete.quests.building_type

import de.westnordost.streetcomplete.quests.building_type.BuildingType.ABANDONED
import de.westnordost.streetcomplete.quests.building_type.BuildingType.ALLOTMENT_HOUSE
import de.westnordost.streetcomplete.quests.building_type.BuildingType.APARTMENTS
import de.westnordost.streetcomplete.quests.building_type.BuildingType.BOATHOUSE
import de.westnordost.streetcomplete.quests.building_type.BuildingType.BRIDGE
import de.westnordost.streetcomplete.quests.building_type.BuildingType.BUNGALOW
import de.westnordost.streetcomplete.quests.building_type.BuildingType.BUNKER
import de.westnordost.streetcomplete.quests.building_type.BuildingType.CARPORT
import de.westnordost.streetcomplete.quests.building_type.BuildingType.CATHEDRAL
import de.westnordost.streetcomplete.quests.building_type.BuildingType.CHAPEL
import de.westnordost.streetcomplete.quests.building_type.BuildingType.CHURCH
import de.westnordost.streetcomplete.quests.building_type.BuildingType.CIVIC
import de.westnordost.streetcomplete.quests.building_type.BuildingType.COLLEGE
import de.westnordost.streetcomplete.quests.building_type.BuildingType.COMMERCIAL
import de.westnordost.streetcomplete.quests.building_type.BuildingType.DETACHED
import de.westnordost.streetcomplete.quests.building_type.BuildingType.DORMITORY
import de.westnordost.streetcomplete.quests.building_type.BuildingType.FARM
import de.westnordost.streetcomplete.quests.building_type.BuildingType.FARM_AUXILIARY
import de.westnordost.streetcomplete.quests.building_type.BuildingType.FIRE_STATION
import de.westnordost.streetcomplete.quests.building_type.BuildingType.GARAGE
import de.westnordost.streetcomplete.quests.building_type.BuildingType.GARAGES
import de.westnordost.streetcomplete.quests.building_type.BuildingType.GOVERNMENT
import de.westnordost.streetcomplete.quests.building_type.BuildingType.GRANDSTAND
import de.westnordost.streetcomplete.quests.building_type.BuildingType.GREENHOUSE
import de.westnordost.streetcomplete.quests.building_type.BuildingType.HANGAR
import de.westnordost.streetcomplete.quests.building_type.BuildingType.HISTORIC
import de.westnordost.streetcomplete.quests.building_type.BuildingType.HOSPITAL
import de.westnordost.streetcomplete.quests.building_type.BuildingType.HOTEL
import de.westnordost.streetcomplete.quests.building_type.BuildingType.HOUSE
import de.westnordost.streetcomplete.quests.building_type.BuildingType.HOUSEBOAT
import de.westnordost.streetcomplete.quests.building_type.BuildingType.HUT
import de.westnordost.streetcomplete.quests.building_type.BuildingType.INDUSTRIAL
import de.westnordost.streetcomplete.quests.building_type.BuildingType.KINDERGARTEN
import de.westnordost.streetcomplete.quests.building_type.BuildingType.KIOSK
import de.westnordost.streetcomplete.quests.building_type.BuildingType.MOSQUE
import de.westnordost.streetcomplete.quests.building_type.BuildingType.OFFICE
import de.westnordost.streetcomplete.quests.building_type.BuildingType.PAGODA
import de.westnordost.streetcomplete.quests.building_type.BuildingType.PARKING
import de.westnordost.streetcomplete.quests.building_type.BuildingType.RELIGIOUS
import de.westnordost.streetcomplete.quests.building_type.BuildingType.RESIDENTIAL
import de.westnordost.streetcomplete.quests.building_type.BuildingType.RETAIL
import de.westnordost.streetcomplete.quests.building_type.BuildingType.ROOF
import de.westnordost.streetcomplete.quests.building_type.BuildingType.RUINS
import de.westnordost.streetcomplete.quests.building_type.BuildingType.SCHOOL
import de.westnordost.streetcomplete.quests.building_type.BuildingType.SEMI_DETACHED
import de.westnordost.streetcomplete.quests.building_type.BuildingType.SERVICE
import de.westnordost.streetcomplete.quests.building_type.BuildingType.SHED
import de.westnordost.streetcomplete.quests.building_type.BuildingType.SHRINE
import de.westnordost.streetcomplete.quests.building_type.BuildingType.SILO
import de.westnordost.streetcomplete.quests.building_type.BuildingType.SPORTS_CENTRE
import de.westnordost.streetcomplete.quests.building_type.BuildingType.STADIUM
import de.westnordost.streetcomplete.quests.building_type.BuildingType.STATIC_CARAVAN
import de.westnordost.streetcomplete.quests.building_type.BuildingType.STORAGE_TANK
import de.westnordost.streetcomplete.quests.building_type.BuildingType.SYNAGOGUE
import de.westnordost.streetcomplete.quests.building_type.BuildingType.TEMPLE
import de.westnordost.streetcomplete.quests.building_type.BuildingType.TERRACE
import de.westnordost.streetcomplete.quests.building_type.BuildingType.TOILETS
import de.westnordost.streetcomplete.quests.building_type.BuildingType.TRAIN_STATION
import de.westnordost.streetcomplete.quests.building_type.BuildingType.TRANSPORTATION
import de.westnordost.streetcomplete.quests.building_type.BuildingType.UNIVERSITY
import de.westnordost.streetcomplete.quests.building_type.BuildingType.WAREHOUSE

enum class BuildingTypeCategory(val type: BuildingType?, val subTypes: List<BuildingType>) {
    RESIDENTIAL(BuildingType.RESIDENTIAL, listOf(
        DETACHED, APARTMENTS, SEMI_DETACHED, TERRACE, HOUSE, FARM, HUT, BUNGALOW, HOUSEBOAT,
        STATIC_CARAVAN, DORMITORY
    )),
    COMMERCIAL(BuildingType.COMMERCIAL, listOf(
        OFFICE, INDUSTRIAL, RETAIL, WAREHOUSE, KIOSK, HOTEL, STORAGE_TANK, BUNGALOW, BRIDGE
    )),
    CIVIC(BuildingType.CIVIC, listOf(
        SCHOOL, UNIVERSITY, HOSPITAL, KINDERGARTEN, SPORTS_CENTRE, TRAIN_STATION, TRANSPORTATION,
        COLLEGE, GOVERNMENT, STADIUM, FIRE_STATION, OFFICE, GRANDSTAND
    )),
    RELIGIOUS(BuildingType.RELIGIOUS, listOf(
        CHURCH, CATHEDRAL, CHAPEL, MOSQUE, TEMPLE, PAGODA, SYNAGOGUE, SHRINE
    )),
    FOR_CARS(null, listOf(
        GARAGE, GARAGES, CARPORT, PARKING
    )),
    FOR_FARMS(null, listOf(
        FARM, FARM_AUXILIARY, SILO, GREENHOUSE, STORAGE_TANK, SHED, ALLOTMENT_HOUSE
    )),
    OTHER(null, listOf(
        SHED, ROOF, BRIDGE, ALLOTMENT_HOUSE, SERVICE, HUT, TOILETS, HANGAR, BUNKER, HISTORIC, BOATHOUSE,
        ABANDONED, RUINS
    )),
}
