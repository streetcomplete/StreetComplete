package de.westnordost.streetcomplete.util.ktx

import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.osm.IS_AREA_EXPRESSION
import de.westnordost.streetcomplete.osm.isThingOrDisusedThing

fun Element.copy(
    id: Long = this.id,
    tags: Map<String, String> = this.tags,
    version: Int = this.version,
    timestampEdited: Long = this.timestampEdited,
): Element =
    when (this) {
        is Node -> Node(id, position, tags, version, timestampEdited)
        is Way -> Way(id, ArrayList(nodeIds), tags, version, timestampEdited)
        is Relation -> Relation(id, ArrayList(members), tags, version, timestampEdited)
    }

val Element.geometryType: GeometryType get() =
    when {
        type == ElementType.NODE -> GeometryType.POINT
        isArea() -> GeometryType.AREA
        type == ElementType.RELATION -> GeometryType.RELATION
        else -> GeometryType.LINE
    }

fun Element.isArea(): Boolean = when (this) {
    is Way -> isClosed && IS_AREA_EXPRESSION.matches(this)
    is Relation -> tags["type"] == "multipolygon"
    else -> false
}

/** An element is only splittable if it is a way that is either not closed or neither an area
 *  nor a roundabout */
fun Element.isSplittable(): Boolean = when (this) {
    // see #5372 for as to why junction=roundabout is not splittable
    is Way -> !isClosed || (!IS_AREA_EXPRESSION.matches(this) && tags["junction"] != "roundabout")
    else -> false
}

fun Element.couldBeSteps(): Boolean = when (this) {
    is Way -> !isArea() && (tags["highway"] == "footway" || tags["highway"] == "path")
    else -> false
}

/** Whether the user should be able to delete this element instead. Currently, we can only delete
 *  nodes, for technical reasons.
 *
 *  Additionally, we don't want to offer the user to delete things that are expected to be crucial
 *  parts of a relation, a network or are a kind of POI where deletion is not recommended (e.g. a
 *  shop should rather be set to disused:shop=yes until there is another one).
 *
 *  So, mainly "things".
 *  */
fun Element.isDeletable(): Boolean =
    (this is Node) &&
    // we rather want the user to leave a note with photo in that case
    (tags["seasonal"] == null || tags["seasonal"] == "no") &&
    (
        isThingOrDisusedThing() ||
        // barriers like bollards, gates, … are not things, but should also be deletable
        tags["barrier"] != null
    )
