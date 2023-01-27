package de.westnordost.streetcomplete.quests.sport

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags

class AddSport : OsmFilterQuestType<List<Sport>>() {

    override val elementFilter = """
        ways with
          leisure = pitch
          and (!sport or sport ~ football|skating|hockey|team_handball)
          and access !~ private|no
    """
    /* treat ambiguous values as if it is not set */
    override val changesetComment = "Specify sport played on pitches"
    override val wikiLink = "Key:sport"
    override val icon = R.drawable.ic_quest_sport
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_sport_title

    override fun createForm() = AddSportForm()

    override fun applyAnswerTo(answer: List<Sport>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["sport"] = answer.joinToString(";") { it.osmValue }
    }
}
