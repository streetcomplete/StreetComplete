package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.expandSides
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.mergeSides
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.osm.updateCheckDateForKey

fun SidewalkSurface.applyTo(tags: Tags) {

    val sidewalksSides = parseSidewalkSides(tags)

    tags.expandSides("sidewalk", "surface")
    tags.expandSides("sidewalk", "surface:note")
    tags.expandSides("sidewalk", "smoothness")

    value.left?.applyTo(tags, "sidewalk:left", updateCheckDate = false)
    value.right?.applyTo(tags, "sidewalk:right", updateCheckDate = false)

    if (sidewalksSides?.left != Sidewalk.YES) tags.removeSidewalkSurfaceTags("left")
    if (sidewalksSides?.right != Sidewalk.YES) tags.removeSidewalkSurfaceTags("right")

    tags.mergeSides("sidewalk", "surface")
    tags.mergeSides("sidewalk", "surface:note")
    tags.mergeSides("sidewalk", "smoothness")

    if (!tags.hasChanges || tags.hasCheckDateForKey("sidewalk:surface")) {
        tags.updateCheckDateForKey("sidewalk:surface")
    }
}

// Side has no sidewalk on this way (no, or separately mapped), so it must not carry surface tags.
private fun Tags.removeSidewalkSurfaceTags(side: String) {
    remove("sidewalk:$side:surface")
    remove("sidewalk:$side:surface:note")
    remove("sidewalk:$side:smoothness")
}
