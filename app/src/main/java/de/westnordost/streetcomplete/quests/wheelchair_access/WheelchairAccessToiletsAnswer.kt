package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

sealed interface WheelchairAccessToiletsAnswer
data class WheelchairAccessToilets(val access: WheelchairAccess) : WheelchairAccessToiletsAnswer
object NoToilet : WheelchairAccessToiletsAnswer

fun WheelchairAccessToiletsAnswer.applyTo(tags: Tags) = when (this) {
    is WheelchairAccessToilets -> {
        tags.updateWithCheckDate("toilets:wheelchair", access.osmValue)
        tags["toilets"] = "yes"
    }
    NoToilet -> {
        tags.updateWithCheckDate("toilets", "no")
    }
}
