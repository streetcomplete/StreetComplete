package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression

/** Expression to see if an element is an area. disused:X is an area too if X is an area. */
val IS_AREA_EXPRESSION = """
    ways with
      area = yes
      or area != no and (
        ${isAreaExpressionFragment()}
        or ~"disused:.*" and (${isAreaExpressionFragment("disused")})
      )
""".toElementFilterExpression()

private fun isAreaExpressionFragment(prefix: String? = null): String {
    val p = if (prefix != null) "$prefix:" else ""
    /* roughly sorted by occurrence count */
    return """
        ${p}building
        or ${p}landuse
        or ${p}landcover
        or ${p}natural ~ wood|scrub|heath|moor|grassland|fell|bare_rock|scree|shingle|sand|mud|water|wetland|glacier|beach|rock|sinkhole
        or ${p}amenity
        or (${p}leisure and ${p}leisure != track)
        or ${p}shop
        or ${p}man_made ~ beacon|bridge|campanile|dolphin|lighthouse|obelisk|observatory|tower|bunker_silo|chimney|gasometer|kiln|mineshaft|petroleum_well|silo|storage_tank|watermill|windmill|works|communications_tower|monitoring_station|street_cabinet|pumping_station|reservoir_covered|wastewater_plant|water_tank|water_tower|water_well|water_works
        or ${p}boundary
        or ${p}tourism
        or ${p}building:part
        or ${p}place
        or ${p}power ~ compensator|converter|generator|plant|substation
        or ${p}aeroway
        or ${p}historic
        or ${p}public_transport
        or ${p}office
        or (${p}emergency and ${p}emergency !~ yes|no)
        or ${p}railway ~ platform|station
        or ${p}craft
        or ${p}waterway ~ boatyard|dam|dock|riverbank|fuel
        or ${p}cemetery ~ sector|grave
        or (${p}military and ${p}military != trench)
        or ${p}aerialway = station
        or ${p}allotments
    """.trimIndent()
}
