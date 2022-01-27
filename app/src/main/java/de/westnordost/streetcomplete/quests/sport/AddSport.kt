package de.westnordost.streetcomplete.quests.sport

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.OUTDOORS

class AddSport : OsmFilterQuestType<List<Sport>>() {

    private val ambiguousSportValues = listOf(
        "team_handball", // -> not really ambiguous but same as handball
        "hockey", // -> ice_hockey or field_hockey
        "skating", // -> ice_skating or roller_skating
        "football" // -> american_football, soccer or other *_football
    )

    override val elementFilter = """
        ways with
          leisure = pitch
          and (!sport or sport ~ ${ambiguousSportValues.joinToString("|")} )
          and access !~ private|no
    """
    override val changesetComment = "Add pitches sport"
    override val wikiLink = "Key:sport"
    override val icon = R.drawable.ic_quest_sport

    override val questTypeAchievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_sport_title

    override fun createForm() = AddSportForm()

    override fun applyAnswerTo(answer: List<Sport>, tags: Tags, timestampEdited: Long) {
        tags["sport"] = answer.joinToString(";") { it.osmValue }
    }
}
