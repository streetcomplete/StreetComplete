package de.westnordost.streetcomplete.quests.width

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker

class AddCyclewayWidth(
    private val checkArSupport: ArSupportChecker
) : OsmFilterQuestType<WidthAnswer>() {

    /* All either exclusive cycleways or ways that are cycleway + footway (or bridleway) but
     *  segregated */
    override val elementFilter = """
        ways with (
          (
            highway = cycleway
            and foot !~ yes|designated
            and (!width or source:width ~ ".*estimat.*")
          ) or (
            segregated = yes
            and (
              highway = cycleway and foot ~ yes|designated
              or highway ~ path|footway and bicycle != no
              or highway = bridleway and bicycle ~ designated|yes
            )
            and (!cycleway:width or source:cycleway:width ~ ".*estimat.*")
          )
        )
        and area != yes
        and access !~ private|no
        and placement != transition
    """
    override val changesetComment = "Specify cycleways width"
    override val wikiLink = "Key:width"
    override val icon = R.drawable.ic_quest_bicycleway_width
    override val achievements = listOf(BICYCLIST)
    override val defaultDisabledMessage: Int
        get() = if (!checkArSupport()) R.string.default_disabled_msg_no_ar else 0

    override fun getTitle(tags: Map<String, String>) = R.string.quest_cycleway_width_title

    override fun createForm() = AddWidthForm()

    override fun applyAnswerTo(answer: WidthAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        val isExclusive = tags["highway"] == "cycleway" && tags["foot"] != "yes" && tags["foot"] != "designated"

        val key = if (isExclusive) "width" else "cycleway:width"

        tags[key] = answer.width.toOsmValue()
        if (answer.isARMeasurement) {
            tags["source:$key"] = "ARCore"
        } else {
            tags.remove("source:$key")
        }
    }
}
