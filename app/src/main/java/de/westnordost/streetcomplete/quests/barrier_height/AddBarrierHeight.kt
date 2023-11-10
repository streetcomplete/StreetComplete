package de.westnordost.streetcomplete.quests.barrier_height

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker

class AddBarrierHeight(
    private val checkArSupport: ArSupportChecker
) : OsmFilterQuestType<BarrierHeightAnswer>() {

    override val elementFilter = """
        ways with
        barrier ~ fence|guard_rail|handrail|hedge|wall|cable_barrier
        and !height
    """

    override val changesetComment = "Specify barrier heights"
    override val wikiLink = "Key:height"
    override val icon = R.drawable.ic_quest_barrier_height
    override val achievements = listOf(EditTypeAchievement.PEDESTRIAN)
    override val defaultDisabledMessage: Int
        get() = if (!checkArSupport()) R.string.default_disabled_msg_no_ar else R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>): Int {
        return R.string.quest_barrier_height_title
    }

    override fun createForm() = AddBarrierHeightForm()

    override fun applyAnswerTo(answer: BarrierHeightAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["height"] = answer.height.toOsmValue()
        if (answer.isARMeasurement) {
            tags["source:height"] = "ARCore"
        } else {
            tags.remove("source:height")
        }
    }
}
