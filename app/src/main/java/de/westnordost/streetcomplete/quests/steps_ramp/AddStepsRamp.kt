package de.westnordost.streetcomplete.quests.steps_ramp

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.ktx.toYesNo

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

    override val commitMessage = "Add whether steps have a ramp"
    override val wikiLink = "Key:ramp"
    override val icon = R.drawable.ic_quest_steps_ramp
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_steps_ramp_title

    override fun createForm() = AddStepsRampForm()

    override fun applyAnswerTo(answer: StepsRampAnswer, changes: StringMapChangesBuilder) {
        // special tagging if the wheelchair ramp is separate
        if (answer.wheelchairRamp == WheelchairRampStatus.SEPARATE) {
            val hasAnotherRamp = answer.bicycleRamp || answer.strollerRamp
            // there is just a separate wheelchair ramp -> use ramp=separate, otherwise just yes
            changes.updateWithCheckDate("ramp", if (!hasAnotherRamp) "separate" else "yes")
            changes.applyRampAnswer("bicycle", answer.bicycleRamp)
            changes.applyRampAnswer("stroller", answer.strollerRamp)
            changes.addOrModify("ramp:wheelchair", "separate")
        } else {
            // updating ramp key: We need to take into account other ramp:*=yes values not touched
            // by this app
            val supportedRampKeys = listOf("ramp:wheelchair", "ramp:stroller", "ramp:bicycle")
            val anyUnsupportedRampTagIsYes = changes.getPreviousEntries().filterKeys {
                it.startsWith("ramp:") && !supportedRampKeys.contains(it)
            }.any { it.value != "no" }

            val hasRamp = (answer.hasRamp() || anyUnsupportedRampTagIsYes)
            changes.updateWithCheckDate("ramp", hasRamp.toYesNo())

            val hasWheelchairRamp = answer.wheelchairRamp != WheelchairRampStatus.NO
            changes.applyRampAnswer("bicycle", answer.bicycleRamp)
            changes.applyRampAnswer("stroller", answer.strollerRamp)
            changes.applyRampAnswer("wheelchair", hasWheelchairRamp)
        }
    }
}

private fun StringMapChangesBuilder.applyRampAnswer(rampType: String, hasRamp: Boolean) {
    if (hasRamp) {
        addOrModify("ramp:$rampType", "yes")
    } else if(getPreviousValue("ramp:$rampType") in listOf("yes", "separate")) {
        delete("ramp:$rampType")
    }
}