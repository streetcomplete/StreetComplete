package de.westnordost.streetcomplete.quests.steps_ramp

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.*


class AddStepsRamp(o: OverpassMapDataAndGeometryApi, r: ResurveyIntervalsStore)
    : SimpleOverpassQuestType<StepsRamp>(o) {

    override val tagFilters = """
        ways with highway = steps
         and (!indoor or indoor = no)
         and access !~ private|no
         and (!conveying or conveying = no)
         and ramp != separate
         and (
           !ramp
           or (ramp = yes and !ramp:stroller and !ramp:bicycle and !ramp:wheelchair)
           or ramp = no and ramp older today -${r * 4} years
           or ramp older today -${r * 8} years
         )
    """

    override val commitMessage = "Add whether steps have a ramp"
    override val wikiLink = "Key:ramp"
    override val icon = R.drawable.ic_quest_steps_ramp

    override fun getTitle(tags: Map<String, String>) = R.string.quest_steps_ramp_title

    override fun createForm() = AddStepsRampForm()

    override fun applyAnswerTo(answer: StepsRamp, changes: StringMapChangesBuilder) {
        // TODO test this
        // updating ramp key: We need to take into account other ramp:*=yes values not touched
        // by this app
        val supportedRampKeys = listOf("ramp:wheelchair", "ramp:stroller", "ramp:bicycle")
        val anyUnsupportedRampTagIsYes = changes.getPreviousEntries().filterKeys {
            it.startsWith("ramp:") && !supportedRampKeys.contains(it)
        }.any { it.value == "yes" }
        val hasRamp = (answer != NONE || anyUnsupportedRampTagIsYes)

        changes.updateWithCheckDate("ramp", hasRamp.toYesNo())

        when(answer) {
            NONE -> {
                changes.addOrModify("wheelchair", "no")
                changes.deleteIfExists("ramp:wheelchair")
                changes.deleteIfExists("ramp:stroller")
                changes.deleteIfExists("ramp:bicycle")
            }
            BICYCLE -> {
                changes.addOrModify("wheelchair", "no")
                changes.addOrModify("ramp:wheelchair", "no")
                changes.addOrModify("ramp:stroller", "no")
                changes.addOrModify("ramp:bicycle", "yes")
            }
            STROLLER -> {
                changes.addOrModify("wheelchair", "no")
                changes.addOrModify("ramp:wheelchair", "no")
                changes.addOrModify("ramp:stroller", "yes")
                changes.addOrModify("ramp:bicycle", "yes")
            }
            WHEELCHAIR -> {
                changes.addOrModify("wheelchair", "yes")
                changes.addOrModify("ramp:wheelchair", "yes")
                changes.addOrModify("ramp:stroller", "yes")
                changes.addOrModify("ramp:bicycle", "yes")
            }
        }
    }
}