package de.westnordost.streetcomplete.quests.incline_direction

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.incline_direction.InclineDirection.UP
import de.westnordost.streetcomplete.quests.incline_direction.InclineDirection.UP_REVERSED

class AddBicycleIncline : OsmFilterQuestType<InclineDirection>() {

    override val elementFilter = """
        ways with mtb:scale:uphill
         and highway ~ footway|cycleway|path|bridleway|track
         and (!indoor or indoor = no)
         and area != yes
         and access !~ private|no
         and !incline
    """
    override val changesetComment = "Specify which way leads up (where mtb:scale:uphill is present)"
    override val wikiLink = "Key:incline"
    override val icon = R.drawable.ic_quest_bicycle_incline
    override val achievements = listOf(BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_incline_title

    override fun createForm() = AddInclineForm()

    override fun applyAnswerTo(answer: InclineDirection, tags: Tags, timestampEdited: Long) {
        tags["incline"] = when (answer) {
            UP -> "up"
            UP_REVERSED -> "down"
        }
    }
}
