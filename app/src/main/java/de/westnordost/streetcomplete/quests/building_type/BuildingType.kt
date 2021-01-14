package de.westnordost.streetcomplete.quests.building_type

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
    TRAIN_STATION ("building", "train_station"),
    TRANSPORTATION("building", "transportation"),
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
    ROOF          ("building", "roof"),
    TOILETS       ("building", "toilets"),
    SERVICE       ("building", "service"),
    HANGAR        ("building", "hangar"),
    BUNKER        ("building", "bunker"),
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

val topBuildingTypes = listOf(
    BuildingType.DETACHED, BuildingType.APARTMENTS, BuildingType.HOUSE, BuildingType.GARAGE,
    BuildingType.SHED, BuildingType.HUT
)

enum class BuildingTypeCategory(val type: BuildingType?, val subTypes: List<BuildingType>) {
    RESIDENTIAL(BuildingType.RESIDENTIAL, listOf(
        BuildingType.DETACHED, BuildingType.APARTMENTS, BuildingType.SEMI_DETACHED,
        BuildingType.TERRACE, BuildingType.HOUSE, BuildingType.FARM, BuildingType.HUT,
        BuildingType.BUNGALOW, BuildingType.HOUSEBOAT, BuildingType.STATIC_CARAVAN,
        BuildingType.DORMITORY
    )),
    COMMERCIAL(BuildingType.COMMERCIAL, listOf(
        BuildingType.OFFICE, BuildingType.INDUSTRIAL, BuildingType.RETAIL, BuildingType.WAREHOUSE,
        BuildingType.KIOSK, BuildingType.HOTEL, BuildingType.STORAGE_TANK
    )),
    CIVIC(BuildingType.CIVIC, listOf(
        BuildingType.SCHOOL, BuildingType.UNIVERSITY, BuildingType.HOSPITAL,
        BuildingType.KINDERGARTEN, BuildingType.SPORTS_CENTRE, BuildingType.TRAIN_STATION,
        BuildingType.TRANSPORTATION, BuildingType.COLLEGE, BuildingType.GOVERNMENT,
        BuildingType.STADIUM
    )),
    RELIGIOUS(BuildingType.RELIGIOUS, listOf(
        BuildingType.CHURCH, BuildingType.CATHEDRAL, BuildingType.CHAPEL, BuildingType.MOSQUE,
        BuildingType.TEMPLE, BuildingType.PAGODA, BuildingType.SYNAGOGUE, BuildingType.SHRINE
    )),
    FOR_CARS(null, listOf(
        BuildingType.GARAGE, BuildingType.GARAGES, BuildingType.CARPORT, BuildingType.PARKING
    )),
    FOR_FARMS(null, listOf(
        BuildingType.FARM, BuildingType.FARM_AUXILIARY, BuildingType.SILO, BuildingType.GREENHOUSE,
        BuildingType.STORAGE_TANK
    )),
    OTHER(null, listOf(
        BuildingType.SHED, BuildingType.ROOF, BuildingType.SERVICE, BuildingType.HUT,
        BuildingType.TOILETS, BuildingType.HANGAR, BuildingType.BUNKER, BuildingType.HISTORIC,
        BuildingType.ABANDONED, BuildingType.RUINS
    )),
}

val buildingCategories = BuildingTypeCategory.values()
