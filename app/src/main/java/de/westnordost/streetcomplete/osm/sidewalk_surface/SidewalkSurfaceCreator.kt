package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.expandSides
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.mergeSides
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.osm.updateCheckDateForKey

fun LeftAndRightSidewalkSurface.applyTo(tags: Tags) {
    tags.expandSides("sidewalk", "surface")
    tags.expandSides("sidewalk", "surface:note")
    tags.expandSides("sidewalk", "smoothness")

    left?.applyTo(tags, "sidewalk:left", updateCheckDate = false)
    right?.applyTo(tags, "sidewalk:right", updateCheckDate = false)

    tags.mergeSides("sidewalk", "surface")
    tags.mergeSides("sidewalk", "surface:note")
    tags.mergeSides("sidewalk", "smoothness")

    if (!tags.hasChanges || tags.hasCheckDateForKey("sidewalk:surface")) {
        tags.updateCheckDateForKey("sidewalk:surface")
    }
}
