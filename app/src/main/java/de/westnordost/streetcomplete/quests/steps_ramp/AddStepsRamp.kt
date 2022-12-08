package de.westnordost.streetcomplete.quests.steps_ramp

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddStepsRamp : OsmFilterQuestType<StepsRampAnswer>() {

    override val elementFilter = """
        ways with highway = steps
         and (!indoor or indoor = no)
         and access !~ private|no
         and (!conveying or conveying = no)
         and ramp != separate
         and (
           !ramp
           or (ramp = yes and !ramp:stroller and !ramp:bicycle and !ramp:wheelchair)
           or ramp = no and ramp older today -4 years
           or ramp older today -8 years
         )
    """
    override val changesetComment = "Specify whether steps have a ramp"
    override val wikiLink = "Key:ramp"
    override val icon = R.drawable.ic_quest_steps_ramp
    override val achievements = listOf(PEDESTRIAN, WHEELCHAIR, BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_steps_ramp_title

    override fun createForm() = AddStepsRampForm()

    override fun applyAnswerTo(answer: StepsRampAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        // special tagging if the wheelchair ramp is separate
        if (answer.wheelchairRamp == WheelchairRampStatus.SEPARATE) {
            val hasAnotherRamp = answer.bicycleRamp || answer.strollerRamp
            // there is just a separate wheelchair ramp -> use ramp=separate, otherwise just yes
            tags.updateWithCheckDate("ramp", if (!hasAnotherRamp) "separate" else "yes")
            applyRampAnswer(tags, "bicycle", answer.bicycleRamp, false)
            applyRampAnswer(tags, "stroller", answer.strollerRamp, false)
            tags["ramp:wheelchair"] = "separate"
        } else {
            // updating ramp key: We need to take into account other ramp:*=yes values not touched
            // by this app
            val supportedRampKeys = listOf("ramp:wheelchair", "ramp:stroller", "ramp:bicycle")
            val anyUnsupportedRampTagIsYes = tags.entries.any { (key, value) ->
                key.startsWith("ramp:") && key !in supportedRampKeys && value != "no"
            }

            val hasRamp = (answer.hasRamp() || anyUnsupportedRampTagIsYes)
            tags.updateWithCheckDate("ramp", hasRamp.toYesNo())

            val hasWheelchairRamp = answer.wheelchairRamp != WheelchairRampStatus.NO
            applyRampAnswer(tags, "bicycle", answer.bicycleRamp, anyUnsupportedRampTagIsYes)
            applyRampAnswer(tags, "stroller", answer.strollerRamp, anyUnsupportedRampTagIsYes)
            applyRampAnswer(tags, "wheelchair", hasWheelchairRamp, anyUnsupportedRampTagIsYes)
        }
    }
}

private fun applyRampAnswer(tags: Tags, rampType: String, hasRamp: Boolean, rampTagForcedToBeYes: Boolean) {
    if (hasRamp) {
        tags["ramp:$rampType"] = "yes"
    } else if (rampTagForcedToBeYes) {
        /* if there is an unsupported ramp:*=yes tag but at the same time, there is neither a
        *  bicycle, stroller nor wheelchair ramp, the ramp key will remain =yes. But then, nothing
        *  else will be tagged and thus, the quest will still remain. So in this case, we tag
        *  the user's choices as "no" explicitly. See #3115 */
        tags["ramp:$rampType"] = "no"
    } else if (tags["ramp:$rampType"] in listOf("yes", "separate")) {
        tags.remove("ramp:$rampType")
    }
}
