package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddPathSmoothness : OsmFilterQuestType<SmoothnessAnswer>() {

    override val elementFilter = """
        ways with
          highway ~ ${ALL_PATHS_EXCEPT_STEPS.joinToString("|")}
          and surface ~ ${SURFACES_FOR_SMOOTHNESS.joinToString("|")}
          and access !~ private|no
          and segregated != yes
          and (!conveying or conveying = no)
          and (!indoor or indoor = no)
          and !cycleway:surface and !footway:surface
          and (
            !smoothness
            or smoothness older today -4 years
            or smoothness:date < today -4 years
          )
    """
    override val changesetComment = "Specify paths smoothness"
    override val wikiLink = "Key:smoothness"
    override val icon = R.drawable.ic_quest_way_surface_detail
    override val achievements = listOf(WHEELCHAIR, BICYCLIST)
    override val defaultDisabledMessage = R.string.default_disabled_msg_difficult_and_time_consuming

    override fun getTitle(tags: Map<String, String>) = R.string.quest_smoothness_title

    override fun createForm() = AddSmoothnessForm()

    override fun applyAnswerTo(answer: SmoothnessAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is SmoothnessValueAnswer -> {
                tags.updateWithCheckDate("smoothness", answer.value.osmValue)
                tags.remove("smoothness:date")
            }
            is WrongSurfaceAnswer -> {
                tags.remove("surface")
                tags.remove("smoothness")
                tags.remove("smoothness:date")
                tags.removeCheckDatesForKey("smoothness")
            }
            is IsActuallyStepsAnswer -> {
                tags["highway"] = "steps"
                tags.remove("smoothness")
                tags.remove("smoothness:date")
                tags.removeCheckDatesForKey("smoothness")
            }
        }
    }
}

// smoothness is not asked for steps
val ALL_PATHS_EXCEPT_STEPS = listOf("footway", "cycleway", "path", "bridleway")
