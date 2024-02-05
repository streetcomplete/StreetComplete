package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element

fun Element.isStreetFurniture(): Boolean =
    IS_STREET_FURNITURE_EXPRESSION.matches(this)

fun Element.isDisusedStreetFurniture(): Boolean =
    this.asIfItWasnt("disused")?.let { IS_STREET_FURNITURE_EXPRESSION.matches(it) } == true

private val IS_STREET_FURNITURE_EXPRESSION by lazy {
    val tags = mapOf(
        "amenity" to listOf(
            "atm",
            "bbq",
            "bench",
            "bicycle_parking",
            "bicycle_rental",
            "bicycle_repair_station",
            "bicycle_wash",
            "charging_station",
            "clock",
            "compressed_air",
            "dog_toilet",
            "drinking_water",
            "give_box",
            "grit_bin",
            "hunting_stand",
            "kneipp_water_cure",
            // "letter_box", - blocked by https://github.com/streetcomplete/StreetComplete/issues/4916
            "locker",
            "lounger",
            "luggage_locker",
            "parcel_locker",
            // "post_box", - blocked by https://github.com/streetcomplete/StreetComplete/issues/4916
            "public_bookcase",
            // "recycling" only for containers
            "sanitary_dump_station",
            "shelter",
            "shower",
            "smoking_area",
            "taxi",
            "telephone",
            "toilets",
            "trolley_bay",
            "vending_machine",
            "waste_basket",
            "water_point",
            "watering_place",
            "device_charging_station",
        ),
        "emergency" to listOf(
            "access_point",
            "assembly_point",
            "defibrillator",
            "fire_hydrant",
            "life_ring",
            "lifeguard",
            "phone",
            "siren",
        ),
        "highway" to listOf(
            "cyclist_waiting_aid",
            "emergency_access_point",
            "milestone",
            "street_lamp",
        ),
        "historic" to listOf(
            "boundary_stone",
            "memorial",
            "monument",
            "wayside_cross",
            "wayside_shrine",
        ),
        "leisure" to listOf(
            "firepit",
            "fitness_station",
            "picnic_table",
        ),
        "man_made" to listOf(
            "cairn",
            "carpet_hanger",
            "cross",
            "flagpole",
            "insect_hotel",
            "monitoring_station",
            "obelisk",
            "planter",
            "snow_cannon",
            // "street_cabinet", - blocked by https://github.com/streetcomplete/StreetComplete/issues/4916
            "surveillance",
            "water_tap",
            "water_well",
        ),
        "natural" to listOf(
            "spring",
            "tree",
            "tree_stump",
        ),
        "tourism" to listOf(
            "artwork",
            "picnic_site",
            "viewpoint",
        )
    )
    .map { it.key + " ~ " + it.value.joinToString("|") }
    .joinToString("\n    or ")

    """
        nodes, ways, relations with
        $tags
        or advertising
        or (amenity = recycling and recycling_type = container)
        or man_made = street_cabinet and street_cabinet != postal_service
        or (tourism = information and information !~ office|visitor_centre)
        or leisure = pitch and sport ~ chess|table_soccer|table_tennis
        or boundary ~ marker
    """.toElementFilterExpression()
}

val POPULAR_STREET_FURNITURE_FEATURE_IDS = listOf(
    // ordered by popularity, skipping trees as there are multiple variants of them
    "highway/street_lamp",      // 3.8M
    "amenity/bench",            // 2.3M
    "emergency/fire_hydrant",   // 1.9M
    "amenity/bicycle_parking",  // 0.6M
    "amenity/shelter",          // 0.5M
    "amenity/toilets",          // 0.4M
    // "amenity/post_box",      // 0.4M
    // blocked by https://github.com/streetcomplete/StreetComplete/issues/4916
    // waiting for response in https://github.com/ideditor/schema-builder/issues/94
    "amenity/drinking_water",   // 0.3M
    "leisure/picnic_table",     // 0.3M

    // popular, a bit less than some competing entries
    // but interesting and worth promoting
    "emergency/defibrillator",  // 0.08M
)
