package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element

fun Element.isStreetFurniture(): Boolean =
    IS_STREET_FURNITURE_EXPRESSION.matches(this)

fun Element.isDisusedStreetFurniture(): Boolean =
    this.asIfItWasnt("disused")?.let { IS_STREET_FURNITURE_EXPRESSION.matches(it) } == true

private val IS_STREET_FURNITURE_EXPRESSION by lazy {
    // note that some entries like amenity=bicycle_wash are not actually appearing in app
    // as there is no matching iD preset
    val amenities = listOf(
        "bicycle_parking", "bicycle_wash", "bicycle_rental", "bench", "lounger", "bbq", "grit_bin",
        "public_bookcase", "give_box", "clock", "bicycle_repair_station", "charging_station",
        "parcel_locker", "telephone", "drinking_water", "vending_machine", "sanitary_dump_station",
        "atm", "waste_basket", "trolley_bay", "hunting_stand", "toilets", "kneipp_water_cure",
        "luggage_locker", "locker", "shelter", "taxi", "shower", "compressed_air",
        "device_charging_station", "water_point", "watering_place", "dog_toilet", "smoking_area",
        // "post_box", "letter_box", - blocked by https://github.com/streetcomplete/StreetComplete/issues/4916
        // waiting for response in https://github.com/ideditor/schema-builder/issues/94
        // man_made = street_cabinet and street_cabinet = postal_service
        // is also disabled to avoid bad data being added
    )
    """
        nodes, ways, relations with
        amenity ~ ${amenities.joinToString("|")}
        or (amenity = recycling and recycling_type = container)
        or leisure ~ picnic_table|firepit|fitness_station
        or man_made ~ water_tap|water_well|obelisk|cross|monitoring_station|flagpole|carpet_hanger|planter|surveillance|insect_hotel|snow_cannon|cairn
        or man_made = street_cabinet and street_cabinet != postal_service
        or tourism ~ viewpoint|artwork|picnic_site
        or (tourism = information and information ~ guidepost|board|map|terminal)
        or historic ~ memorial|monument|wayside_shrine|wayside_cross|boundary_stone
        or highway ~ milestone|street_lamp|emergency_access_point|cyclist_waiting_aid
        or emergency ~ fire_hydrant|life_ring|phone|defibrillator|siren|lifeguard|assembly_point|access_point
        or advertising
        or leisure = pitch and sport ~ table_tennis|chess|table_soccer
        or natural ~ tree|tree_stump|spring
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
