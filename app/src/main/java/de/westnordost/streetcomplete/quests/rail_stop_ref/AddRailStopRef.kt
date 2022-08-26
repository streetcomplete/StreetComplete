package de.westnordost.streetcomplete.quests.rail_stop_ref

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags

class AddRailStopRef : OsmFilterQuestType<RailStopRefAnswer>() {

    override val elementFilter = """
        ways with
        (
          (railway = platform and public_transport = platform)
        )
        and !ref and noref != yes and ref:signed != no and !~"ref:.*"
    """
    override val enabledInCountries = NoCountriesExcept("DE", "FR", "CH")
    override val changesetComment = "Determine Rail Platform refs"
    override val wikiLink = "Tag:public_transport=platform"
    override val icon = R.drawable.ic_quest_railway
    override val achievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_railStopRef_title2

    override fun createForm() = AddRailStopRefForm()

    override fun applyAnswerTo(answer: RailStopRefAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is NoRailStopRef -> tags["ref:signed"] = "no"
            is RailStopRef ->   tags["ref"] = answer.ref
        }
    }
}
