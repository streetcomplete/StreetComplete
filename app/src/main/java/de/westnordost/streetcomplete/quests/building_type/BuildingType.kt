package de.westnordost.streetcomplete.quests.building_type

enum class BuildingType(val osmTag: String?, val subTypes: List<BuildingType>? = null) {
    HOUSE         ("house"),
    APARTMENTS    ("apartments"),
    DETACHED      ("detached"),
    SEMI_DETACHED ("semidetached_house"),
    TERRACE       ("terrace"),
    HOTEL         ("hotel"),
    DORMITORY     ("dormitory"),
    HOUSEBOAT     ("houseboat"),
    BUNGALOW      ("bungalow"),
    STATIC_CARAVAN("static_caravan"),
    HUT           ("hut"),

    INDUSTRIAL    ("industrial"),
    RETAIL        ("retail"),
    OFFICE        ("office"),
    WAREHOUSE     ("warehouse"),
    KIOSK         ("kiosk"),
    STORAGE_TANK  ("man_made=storage_tank"),

    KINDERGARTEN  ("kindergarten"),
    SCHOOL        ("school"),
    COLLEGE       ("college"),
    SPORTS_CENTRE ("sports_centre"),
    HOSPITAL      ("hospital"),
    STADIUM       ("stadium"),
    TRAIN_STATION ("train_station"),
    TRANSPORTATION("transportation"),
    UNIVERSITY    ("university"),
    GOVERNMENT    ("government"),

    CHURCH        ("church"),
    CHAPEL        ("chapel"),
    CATHEDRAL     ("cathedral"),
    MOSQUE        ("mosque"),
    TEMPLE        ("temple"),
    PAGODA        ("pagoda"),
    SYNAGOGUE     ("synagogue"),
    SHRINE        ("shrine"),

    CARPORT       ("carport"),
    GARAGE        ("garage"),
    GARAGES       ("garages"),
    PARKING       ("parking"),

    FARM          ("farm"),
    FARM_AUXILIARY("farm_auxiliary"),
    SILO          ("man_made=silo"),
    GREENHOUSE    ("greenhouse"),

    SHED          ("shed"),
    ROOF          ("roof"),
    TOILETS       ("toilets"),
    SERVICE       ("service"),
    HANGAR        ("hangar"),
    BUNKER        ("bunker"),
    HISTORIC      ("historic"),
    ABANDONED     ("abandoned"),
    RUINS         ("ruins"),

    RESIDENTIAL   ("residential", listOf(DETACHED, APARTMENTS, SEMI_DETACHED, TERRACE, HOUSE, FARM, HUT, BUNGALOW, HOUSEBOAT, STATIC_CARAVAN, DORMITORY)),
    COMMERCIAL    ("commercial", listOf(OFFICE, INDUSTRIAL, RETAIL, WAREHOUSE, KIOSK, HOTEL, STORAGE_TANK)),
    CIVIC         ("civic", listOf(SCHOOL, UNIVERSITY, HOSPITAL, KINDERGARTEN, SPORTS_CENTRE, TRAIN_STATION, TRANSPORTATION, COLLEGE, GOVERNMENT, STADIUM)),
    RELIGIOUS     ("religious", listOf(CHURCH, CATHEDRAL, CHAPEL, MOSQUE, TEMPLE, PAGODA, SYNAGOGUE, SHRINE)),
    FOR_CARS      (null, listOf(GARAGE, GARAGES, CARPORT, PARKING)),
    FOR_FARMS     (null, listOf(FARM, FARM_AUXILIARY, SILO, GREENHOUSE, STORAGE_TANK)),
    OTHER         (null, listOf(SHED, ROOF, SERVICE, HUT, TOILETS, HANGAR, BUNKER, HISTORIC, ABANDONED, RUINS));

    companion object {
        fun getByTag(key: String, value: String): BuildingType? {
            var tag = if (key == "building") value else "$key=$value"
            // synonyms
            if (tag == "semi")        tag = "semidetached_house"
            else if (tag == "public") tag = "civic"

            return values().find { it.item.value == tag }
        }
    }
}
