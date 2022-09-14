package de.westnordost.streetcomplete.quests.building_type

enum class BuildingType(val osmKey: String, val osmValue: String) {
    HOUSE           ("building", "house"),
    APARTMENTS      ("building", "apartments"),
    DETACHED        ("building", "detached"),
    SEMI_DETACHED   ("building", "semidetached_house"),
    TERRACE         ("building", "terrace"),
    HOTEL           ("building", "hotel"),
    DORMITORY       ("building", "dormitory"),
    HOUSEBOAT       ("building", "houseboat"),
    BUNGALOW        ("building", "bungalow"),
    STATIC_CARAVAN  ("building", "static_caravan"),
    HUT             ("building", "hut"),

    INDUSTRIAL      ("building", "industrial"),
    RETAIL          ("building", "retail"),
    OFFICE          ("building", "office"),
    WAREHOUSE       ("building", "warehouse"),
    KIOSK           ("building", "kiosk"),
    STORAGE_TANK    ("man_made", "storage_tank"),

    KINDERGARTEN    ("building", "kindergarten"),
    SCHOOL          ("building", "school"),
    COLLEGE         ("building", "college"),
    SPORTS_CENTRE   ("building", "sports_centre"),
    HOSPITAL        ("building", "hospital"),
    STADIUM         ("building", "stadium"),
    GRANDSTAND      ("building", "grandstand"),
    TRAIN_STATION   ("building", "train_station"),
    TRANSPORTATION  ("building", "transportation"),
    TRANSIT_SHELTER ("amenity", "shelter"),
    FIRE_STATION    ("building", "fire_station"),
    UNIVERSITY      ("building", "university"),
    GOVERNMENT      ("building", "government"),

    CHURCH          ("building", "church"),
    CHAPEL          ("building", "chapel"),
    CATHEDRAL       ("building", "cathedral"),
    MOSQUE          ("building", "mosque"),
    TEMPLE          ("building", "temple"),
    PAGODA          ("building", "pagoda"),
    SYNAGOGUE       ("building", "synagogue"),
    SHRINE          ("building", "shrine"),
    PRESBYTERY      ("building", "presbytery"),

    CARPORT         ("building", "carport"),
    GARAGE          ("building", "garage"),
    GARAGES         ("building", "garages"),
    PARKING         ("building", "parking"),

    FARM            ("building", "farm"),
    FARM_AUXILIARY  ("building", "farm_auxiliary"),
    SILO            ("man_made", "silo"),
    GREENHOUSE      ("building", "greenhouse"),
    BARN            ("building", "barn"),
    COWSHED         ("building", "cowshed"),
    STABLE          ("building", "stable"),
    STY             ("building", "sty"),

    SHED            ("building", "shed"),
    ALLOTMENT_HOUSE ("building", "allotment_house"),
    ROOF            ("building", "roof"),
    BRIDGE          ("building", "bridge"),
    TOILETS         ("building", "toilets"),
    SERVICE         ("building", "service"),
    TRANSFORMER_TOWER ("building", "transformer_tower"),
    HANGAR          ("building", "hangar"),
    BUNKER          ("building", "bunker"),
    BOATHOUSE       ("building", "boathouse"),
    RIDING_HALL     ("building", "riding_hall"),
    SPORTS_HALL     ("building", "sports_hall"),
    DIGESTER        ("building", "digester"),

    HISTORIC        ("historic", "yes"),
    ABANDONED       ("abandoned", "yes"),
    RUINS           ("ruins", "yes"),

    RESIDENTIAL     ("building", "residential"),
    COMMERCIAL      ("building", "commercial"),
    CIVIC           ("building", "civic"),
    RELIGIOUS       ("building", "religious"),

    CONSTRUCTION    ("building", "construction"),
    DEMOLISHED      ("demolished:building", "yes");

    companion object {
        fun getByTag(key: String, value: String): BuildingType? {
            return values().find { it.osmKey == key && it.osmValue == value }
        }

        val topSelectableValues = listOf(DETACHED, APARTMENTS, HOUSE, GARAGE, SHED, HUT)
    }
}
