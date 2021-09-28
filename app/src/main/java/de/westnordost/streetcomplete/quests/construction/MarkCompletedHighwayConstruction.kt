package de.westnordost.streetcomplete.quests.construction

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.meta.updateCheckDate
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR

class MarkCompletedHighwayConstruction : OsmFilterQuestType<CompletedConstructionAnswer>() {

    override val elementFilter = """
        ways with highway = construction
         and (!opening_date or opening_date < today)
         and older today -2 weeks
    """
    override val commitMessage = "Determine whether construction is now completed"
    override val wikiLink = "Tag:highway=construction"
    override val icon = R.drawable.ic_quest_road_construction
    override val hasMarkersAtEnds = true

    override val questTypeAchievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>): Int {
        val isRoad = ALL_ROADS.contains(tags["construction"])
        val isCycleway = tags["construction"] == "cycleway"
        val isFootway = tags["construction"] == "footway"
        val isSteps = tags["construction"] == "steps"

        return when {
            isRoad -> R.string.quest_construction_road_title
            isCycleway -> R.string.quest_construction_cycleway_title
            isFootway -> R.string.quest_construction_footway_title
            isSteps -> R.string.quest_construction_steps_title
            else -> R.string.quest_construction_generic_title
        }
    }

    override fun createForm() = MarkCompletedConstructionForm()

    override fun applyAnswerTo(answer: CompletedConstructionAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is OpeningDateAnswer -> {
                changes.addOrModify("opening_date", answer.date.toCheckDateString())
            }
            is StateAnswer -> {
                if (answer.value) {
                    val value = changes.getPreviousValue("construction") ?: "road"
                    changes.modify("highway", value)
                    deleteTagsDescribingConstruction(changes)
                } else {
                    changes.updateCheckDate()
                }
            }
        }
    }
}
