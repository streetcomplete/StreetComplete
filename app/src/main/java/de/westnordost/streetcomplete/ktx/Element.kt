package de.westnordost.streetcomplete.ktx

import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.*

fun Element.copy(
    newId: Long = id,
    newVersion: Int = version,
    newTimestampEdited: Long = timestampEdited,
    newTags: Map<String, String> = tags,
): Element {
    return when (this) {
        is Node -> Node(newId, position, newTags, newVersion, newTimestampEdited)
        is Way -> Way(newId, ArrayList(nodeIds), newTags, newVersion, newTimestampEdited)
        is Relation -> Relation(newId, ArrayList(members), newTags, newVersion, newTimestampEdited)
    }
}

val Element.geometryType: GeometryType get() =
    when {
        type == ElementType.NODE -> GeometryType.POINT
        isArea() -> GeometryType.AREA
        type == ElementType.RELATION -> GeometryType.RELATION
        else -> GeometryType.LINE
    }

fun Element.isArea(): Boolean {
    return when(this) {
        is Way -> isClosed && IS_AREA_EXPR.matches(this)
        is Relation -> tags["type"] == "multipolygon"
        else -> false
    }
}

private val IS_AREA_EXPR = """
    ways with area = yes or area != no and (
    aeroway
    or amenity
    or boundary
    or building
    or craft
    or (emergency and emergency !~ yes|no)
    or historic
    or landuse
    or (leisure and leisure != track)
    or office
    or place
    or public_transport
    or shop
    or tourism
    or building:part
    or aerialway = station
    or railway ~ platform|station
    or (military and military != trench)
    or power ~ compensator|converter|generator|plant|substation
    or waterway ~ boatyard|dam|dock|riverbank|fuel
    or cemetery ~ sector|grave
    or natural ~ wood|scrub|heath|moor|grassland|fell|bare_rock|scree|shingle|sand|mud|water|wetland|glacier|beach|rock|sinkhole
    or man_made ~ beacon|bridge|campanile|dolphin|lighthouse|obelisk|observatory|tower|bunker_silo|chimney|gasometer|kiln|mineshaft|petroleum_well|silo|storage_tank|watermill|windmill|works|communications_tower|monitoring_station|street_cabinet|pumping_station|reservoir_covered|wastewater_plant|water_tank|water_tower|water_well|water_works
    )""".toElementFilterExpression()

fun Element.isSomeKindOfShop(): Boolean = IS_SOME_KIND_OF_SHOP_EXPR.matches(this)

/** ~ tenant of a normal retail shop area.
 *  So,
 *  - no larger or purpose-built things like malls, cinemas, theatres, car washes, fuel stations,
 *    museums, galleries, zoos, aquariums, bowling alleys...
 *  - no things that are usually not found in normal retail shop areas but in offices:
 *    clinics, doctors, fitness centers, dental technicians...
 *  - nothing that is rather located in an industrial estate like car repair and other types
 *    of workshops (most craft=* other than those where people go to have something repaired or so)
 *  */
private val IS_SOME_KIND_OF_SHOP_EXPR = ("""
    nodes, ways, relations with
      shop and shop !~ no|vacant|mall
      or tourism = information and information = office
      or """ +
    mapOf(
        "amenity" to arrayOf(
            "restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten", "nightclub",
            "bank", "bureau_de_change", "money_transfer", "post_office", "internet_cafe",
            "pharmacy",
            "driving_school",
        ),
        "leisure" to arrayOf(
            "amusement_arcade", "adult_gaming_centre", "tanning_salon",
        ),
        "office" to arrayOf(
            "insurance", "travel_agent", "tax_advisor", "estate_agent", "political_party",
        ),
        "craft" to arrayOf(
            "shoemaker", "tailor", "photographer", "watchmaker", "optician",
            "electronics_repair", "key_cutter",
        )
    ).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n  or ") + "\n"
    ).trimIndent().toElementFilterExpression()
