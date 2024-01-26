package de.westnordost.streetcomplete.quests.segregated

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.ANYTHING_PAVED
import de.westnordost.streetcomplete.quests.segregated.CyclewaySegregation.*

class AddCyclewaySegregation : OsmFilterQuestType<CyclewaySegregation>() {

    override val elementFilter = """
        ways with
        (
          (highway = path and bicycle = designated and foot = designated)
          or (highway = footway and bicycle = designated)
          or (highway = cycleway and foot ~ designated|yes)
          or
            (
            highway ~ path|footway|cycleway
            and (footway:surface or cycleway:surface)
            and foot !~ private|no
            and bicycle !~ private|no
            )
        )
        and surface ~ ${ANYTHING_PAVED.joinToString("|")}
        and area != yes
        and !sidewalk
        and !segregated
        and ~path|footway|cycleway !~ link
    """
    override val changesetComment = "Specify whether combined foot- and cycleways are segregated"
    override val wikiLink = "Key:segregated"
    override val icon = R.drawable.ic_quest_path_segregation
    override val achievements = listOf(BICYCLIST, OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_segregated_title

    override fun createForm() = AddCyclewaySegregationForm()

    override fun applyAnswerTo(answer: CyclewaySegregation, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            YES -> tags["segregated"] = "yes"
            NO -> tags["segregated"] = "no"
            SIDEWALK -> tags["sidewalk"] = "yes"
        }
    }
}
