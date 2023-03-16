package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

sealed interface WheelchairAccessToiletsAnswer
data class WheelchairAccessToilets(val access: WheelchairAccess) : WheelchairAccessToiletsAnswer
object NoToilet : WheelchairAccessToiletsAnswer

fun NoToilet.applyTo(tags: Tags) {
    tags.updateWithCheckDate("toilets", "no")
}
