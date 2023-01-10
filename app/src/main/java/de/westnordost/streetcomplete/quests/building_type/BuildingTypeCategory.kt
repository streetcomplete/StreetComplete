package de.westnordost.streetcomplete.quests.building_type

import de.westnordost.streetcomplete.quests.building_type.BuildingType.*
import de.westnordost.streetcomplete.quests.building_type.BuildingType.BARN
import de.westnordost.streetcomplete.quests.building_type.BuildingType.COWSHED
import de.westnordost.streetcomplete.quests.building_type.BuildingType.DIGESTER
import de.westnordost.streetcomplete.quests.building_type.BuildingType.PRESBYTERY
import de.westnordost.streetcomplete.quests.building_type.BuildingType.RIDING_HALL
import de.westnordost.streetcomplete.quests.building_type.BuildingType.SPORTS_HALL
import de.westnordost.streetcomplete.quests.building_type.BuildingType.STABLE
import de.westnordost.streetcomplete.quests.building_type.BuildingType.STY
import de.westnordost.streetcomplete.quests.building_type.BuildingType.TRANSFORMER_TOWER
import de.westnordost.streetcomplete.quests.building_type.BuildingType.TRANSIT_SHELTER

enum class BuildingTypeCategory(val type: BuildingType?, val subTypes: List<BuildingType>) {
    RESIDENTIAL(BuildingType.RESIDENTIAL, listOf(
        DETACHED, APARTMENTS, SEMI_DETACHED, TERRACE, HOUSE, FARM, HUT, BUNGALOW, HOUSEBOAT,
        STATIC_CARAVAN, DORMITORY
    )),
    COMMERCIAL(BuildingType.COMMERCIAL, listOf(
        OFFICE, RETAIL, KIOSK, INDUSTRIAL, WAREHOUSE, HOTEL, STORAGE_TANK, BRIDGE, DIGESTER
    )),
    CIVIC(BuildingType.CIVIC, listOf(
        SCHOOL, UNIVERSITY, HOSPITAL, KINDERGARTEN, SPORTS_CENTRE, TRAIN_STATION, TRANSPORTATION,
        COLLEGE, GOVERNMENT, STADIUM, FIRE_STATION, OFFICE, GRANDSTAND, SPORTS_HALL, TRANSIT_SHELTER
    )),
    RELIGIOUS(BuildingType.RELIGIOUS, listOf(
        CHURCH, CATHEDRAL, CHAPEL, MOSQUE, TEMPLE, PAGODA, SYNAGOGUE, SHRINE, PRESBYTERY
    )),
    FOR_CARS(null, listOf(
        GARAGE, GARAGES, CARPORT, PARKING
    )),
    FOR_FARMS(null, listOf(
        FARM, FARM_AUXILIARY, SILO, GREENHOUSE, STORAGE_TANK, SHED, ALLOTMENT_HOUSE, BARN, COWSHED, STABLE, STY
    )),
    OTHER(null, listOf(
        SHED, ROOF, GUARDHOUSE, BRIDGE, ALLOTMENT_HOUSE, SERVICE, TRANSFORMER_TOWER, HUT, TENT, TOILETS, HANGAR, BUNKER,
        HISTORIC, BOATHOUSE, RIDING_HALL, ABANDONED, RUINS
    )),
}
