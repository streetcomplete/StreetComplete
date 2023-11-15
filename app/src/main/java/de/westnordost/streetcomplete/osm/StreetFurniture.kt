package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression

fun isStreetFurnitureFragment(prefix: String? = null): String {
    // note that some entries like amenity=bicycle_wash are not actually appearing in app
    // as there is no matching iD preset
    val amenities = listOf(
        "bicycle_parking", "bicycle_wash", "bicycle_rental", "bench", "lounger", "bbq", "grit_bin",
        "public_bookcase", "give_box", "clock", "bicycle_repair_station", "charging_station",
        "parcel_locker", "telephone", "drinking_water", "vending_machine",
        "atm", "waste_basket", "trolley_bay", "hunting_stand", "toilets",
        // "post_box", "letter_box", - blocked by https://github.com/streetcomplete/StreetComplete/issues/4916
        // waiting for response in https://github.com/ideditor/schema-builder/issues/94
        // man_made = street_cabinet and street_cabinet = postal_service
        // is also disabled to avoid bad data being added
    )
    val p = if (prefix != null) "$prefix:" else ""
    return ("""(
        ${p}amenity ~ ${amenities.joinToString("|")}
        or (${p}amenity = recycling and recycling_type = container)
        or ${p}leisure ~ picnic_table|firepit
        or ${p}man_made ~ water_tap|obelisk|cross|monitoring_station|flagpole|carpet_hanger|planter
        or ${p}tourism ~ viewpoint|artwork
        or (${p}tourism = information and information ~ guidepost|board|map|terminal)
        or ${p}historic ~ memorial|monument|wayside_shrine|wayside_cross|boundary_stone
        or ${p}highway ~ milestone|street_lamp|emergency_access_point
        or ${p}emergency ~ fire_hydrant|life_ring|phone|defibrillator|siren|lifeguard|assembly_point|access_point
        or ${p}advertising
        or ${p}leisure = pitch and sport ~ table_tennis|chess
        or ${p}natural ~ tree
        or ${p}man_made = street_cabinet and street_cabinet != postal_service
        )""")
}

val IS_REGULAR_STREET_FURNITURE_EXPRESSION = """
    nodes, ways, relations with
      ${isStreetFurnitureFragment("")}
""".toElementFilterExpression()

val IS_DISUSED_STREET_FURNITURE_EXPRESSION = """
    nodes, ways, relations with
      ${isStreetFurnitureFragment("disused")}
""".toElementFilterExpression()

val IS_STREET_FURNITURE_INCLUDING_DISUSED_EXPRESSION = """
    nodes, ways, relations with
      ${isStreetFurnitureFragment()}
      or ${isStreetFurnitureFragment("disused")}
""".toElementFilterExpression()
