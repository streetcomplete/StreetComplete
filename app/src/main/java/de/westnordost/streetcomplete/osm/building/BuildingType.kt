package de.westnordost.streetcomplete.osm.building

enum class BuildingType(val osmKey: String?, val osmValue: String?) {
    UNSUPPORTED     (null, null),

    HISTORIC        ("historic", "yes"),
    ABANDONED       ("abandoned", "yes"),
    RUINS           ("ruins", "yes"),

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
    GUARDHOUSE      ("building", "guardhouse"),

    KINDERGARTEN    ("building", "kindergarten"),
    SCHOOL          ("building", "school"),
    COLLEGE         ("building", "college"),
    SPORTS_CENTRE   ("building", "sports_centre"),
    HOSPITAL        ("building", "hospital"),
    STADIUM         ("building", "stadium"),
    GRANDSTAND      ("building", "grandstand"),
    TRAIN_STATION   ("building", "train_station"),
    TRANSPORTATION  ("building", "transportation"),
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

    CARPORT         ("building", "carport"),
    GARAGE          ("building", "garage"),
    GARAGES         ("building", "garages"),
    PARKING         ("building", "parking"),

    FARM            ("building", "farm"),
    FARM_AUXILIARY  ("building", "farm_auxiliary"),
    SILO            ("man_made", "silo"),
    GREENHOUSE      ("building", "greenhouse"),

    OUTBUILDING     ("building", "outbuilding"),
    SHED            ("building", "shed"),
    ALLOTMENT_HOUSE ("building", "allotment_house"),
    ROOF            ("building", "roof"),
    BRIDGE          ("building", "bridge"),
    TOILETS         ("building", "toilets"),
    SERVICE         ("building", "service"),
    HANGAR          ("building", "hangar"),
    BUNKER          ("building", "bunker"),
    BOATHOUSE       ("building", "boathouse"),
    CONTAINER       ("building", "container"),
    TENT            ("building", "tent"),
    TOMB            ("building", "tomb"),
    TOWER           ("man_made", "tower"),

    RESIDENTIAL     ("building", "residential"),
    COMMERCIAL      ("building", "commercial"),
    CIVIC           ("building", "civic"),
    RELIGIOUS       ("building", "religious"),

    CONSTRUCTION    ("building", "construction");

    companion object {
        val topSelectableValues = listOf(DETACHED, APARTMENTS, HOUSE, GARAGE, SHED, HUT)

        /** a map of tag to [BuildingType] of building features that should be treated as aliases
         *  of known building types, i.e. that are displayed as that building type but whose tag is
         *  not modified when saving it again. */
        val aliases: Map<Pair<String, String>, BuildingType> = mapOf(
            // for better overview, this list is sorted by how the values appear on the wiki page
            // for Key:building (if they do appear there)

            // Accommodation
            ("building" to "cabin") to BUNGALOW,
            ("building" to "chalet") to BUNGALOW, // not documented
            ("building" to "summer_cottage") to BUNGALOW, // not documented
            ("building" to "cottage") to BUNGALOW, // not documented
            ("building" to "ger") to TENT, // a Mongolian tent
            ("building" to "stilt_house") to HOUSE,
            ("building" to "terrace_house") to HOUSE,  // not documented, auto-changing to house would be a loss of information
            ("building" to "trullo") to HUT,
            ("building" to "pajaru") to HUT, // not documented, but similar to trullo https://it.wikipedia.org/wiki/Pajaru

            // Commercial
            ("building" to "manufacture") to INDUSTRIAL, // not documented on main page
            ("building" to "factory") to INDUSTRIAL, // not documented on main page
            ("building" to "supermarket") to RETAIL,
            ("building" to "restaurant") to RETAIL, // not documented
            ("building" to "pub") to RETAIL, // not documented
            ("building" to "bank") to RETAIL, // not documented

            // Religious
            ("building" to "kingdom_hall") to RELIGIOUS,
            ("building" to "monastery") to RELIGIOUS,
            ("building" to "presbytery") to RELIGIOUS,
            ("building" to "wayside_shrine") to RELIGIOUS,
            ("building" to "convent") to RELIGIOUS,

            // Civic
            ("building" to "museum") to CIVIC,
            ("building" to "public") to CIVIC, // pretty much a real synonym
            ("building" to "government_office") to GOVERNMENT, // not documented
            ("building" to "education") to CIVIC, // not documented (but I like this tag!!)
            ("building" to "townhall") to CIVIC,
            ("building" to "administrative") to GOVERNMENT, // not documented; =government is also for provincial administration

            // Agricultural
            ("building" to "barn") to FARM_AUXILIARY,
            ("building" to "cowshed") to FARM_AUXILIARY,
            ("building" to "stable") to FARM_AUXILIARY,
            ("building" to "sty") to FARM_AUXILIARY,
            ("building" to "livestock") to FARM_AUXILIARY,
            ("building" to "poultry_house") to FARM_AUXILIARY, // not documented
            ("building" to "chicken_coop") to FARM_AUXILIARY, // not documented
            ("building" to "granary") to SILO, // not documented

            ("building" to "slurry_tank") to STORAGE_TANK,
            ("building" to "digester") to STORAGE_TANK,
            ("building" to "gasometer") to STORAGE_TANK,
            ("building" to "agricultural") to FARM_AUXILIARY, // not documented

            // Technical
            ("building" to "tech_cab") to SERVICE,
            ("building" to "transformer_tower") to SERVICE,
            ("building" to "power_substation") to SERVICE,
            ("building" to "tower") to TOWER, // not really a synonym though
            ("building" to "communications_tower") to TOWER,
            ("building" to "lighthouse") to TOWER,
            ("building" to "water_tower") to TOWER,
            ("building" to "cooling_tower") to TOWER,

            // Other
            ("building" to "castle") to HISTORIC,
            ("building" to "canopy") to ROOF, // not documented
            ("man_made" to "canopy") to ROOF, // not documented
            ("building" to "gazebo") to ROOF, // not documented
        )

        /** a set of building values that should be treated as deprecated aliases of known building
         *  types */
        val deprecatedValues = setOf(
            "semi",
            "semidetached",
            "semi_detached",
            "duplex", // -> semidetached_house

            "detached_house", // -> detached

            "terraced_house",
            "terraced",
            "townhouse", // -> terrace

            "mobile_home", // -> static_caravan

            "flats", // -> apartments

            "shop", // -> retail

            "storage_tank", // -> man_made=storage_tank
            "tank", // -> man_made=storage_tank
            "silo", // -> man_made=silo

            "glasshouse", // ambiguous: could be greenhouse or conservatory

            "collapsed",
            "damaged",
            "ruins", // (not explicitly deprecated though)
            "ruin", // -> ruins=yes

            "abandoned",
            "disused", // -> abandoned=yes

            "unclassified",
            "undefined",
            "unknown",
            "other",
            "fixme", // -> yes
        )

        /** other OSM keys that may describe the purpose of a building */
        val otherKeysPotentiallyDescribingBuildingType = listOf(
            // See #1854, #1891, #3233
            "man_made",
            "historic",
            "military",
            "power",
            "tourism",
            "attraction",
            "amenity",
            "leisure",
            "aeroway",
            "railway",
            "craft",
            "healthcare",
            "office",
            "shop",
            "description",
            "emergency",
        )
    }
}
