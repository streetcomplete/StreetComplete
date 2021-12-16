package de.westnordost.streetcomplete.quests.building_type

import de.westnordost.streetcomplete.quests.building_type.BuildingType.*

enum class BuildingType(val osmKey: String, val osmValue: String) {
    HOUSE         ("building", "house"),
    APARTMENTS    ("building", "apartments"),
    DETACHED      ("building", "detached"),
    SEMI_DETACHED ("building", "semidetached_house"),
    TERRACE       ("building", "terrace"),
    HOTEL         ("building", "hotel"),
    DORMITORY     ("building", "dormitory"),
    HOUSEBOAT     ("building", "houseboat"),
    BUNGALOW      ("building", "bungalow"),
    STATIC_CARAVAN("building", "static_caravan"),
    HUT           ("building", "hut"),

    INDUSTRIAL    ("building", "industrial"),
    RETAIL        ("building", "retail"),
    OFFICE        ("building", "office"),
    WAREHOUSE     ("building", "warehouse"),
    KIOSK         ("building", "kiosk"),
    STORAGE_TANK  ("man_made", "storage_tank"),

    KINDERGARTEN  ("building", "kindergarten"),
    SCHOOL        ("building", "school"),
    COLLEGE       ("building", "college"),
    SPORTS_CENTRE ("building", "sports_centre"),
    HOSPITAL      ("building", "hospital"),
    STADIUM       ("building", "stadium"),
    GRANDSTAND    ("building", "grandstand"),
    TRAIN_STATION ("building", "train_station"),
    TRANSPORTATION("building", "transportation"),
    FIRE_STATION  ("building", "fire_station"),
    UNIVERSITY    ("building", "university"),
    GOVERNMENT    ("building", "government"),

    CHURCH        ("building", "church"),
    CHAPEL        ("building", "chapel"),
    CATHEDRAL     ("building", "cathedral"),
    MOSQUE        ("building", "mosque"),
    TEMPLE        ("building", "temple"),
    PAGODA        ("building", "pagoda"),
    SYNAGOGUE     ("building", "synagogue"),
    SHRINE        ("building", "shrine"),

    CARPORT       ("building", "carport"),
    GARAGE        ("building", "garage"),
    GARAGES       ("building", "garages"),
    PARKING       ("building", "parking"),

    FARM          ("building", "farm"),
    FARM_AUXILIARY("building", "farm_auxiliary"),
    SILO          ("man_made", "silo"),
    GREENHOUSE    ("building", "greenhouse"),

    SHED          ("building", "shed"),
    ALLOTMENT_HOUSE("building", "allotment_house"),
    ROOF          ("building", "roof"),
    BRIDGE        ("building", "bridge"),
    TOILETS       ("building", "toilets"),
    SERVICE       ("building", "service"),
    HANGAR        ("building", "hangar"),
    BUNKER        ("building", "bunker"),
    BOATHOUSE     ("building", "boathouse"),

    HISTORIC      ("historic", "yes"),
    ABANDONED     ("abandoned", "yes"),
    RUINS         ("ruins", "yes"),

    RESIDENTIAL   ("building", "residential"),
    COMMERCIAL    ("building", "commercial"),
    CIVIC         ("building", "civic"),
    RELIGIOUS     ("building", "religious"),

    CONSTRUCTION ("building", "construction");

    companion object {
        fun getByTag(key: String, value: String): BuildingType? {
            return values().find { it.osmKey == key && it.osmValue == value }
        }
    }
}

val topBuildingTypes = listOf(DETACHED, APARTMENTS, HOUSE, GARAGE, SHED, HUT)

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

val buildingCategories = BuildingTypeCategory.values()
