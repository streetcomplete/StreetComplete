package de.westnordost.streetcomplete.quests.toilet_availability

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddToiletAvailability : OsmFilterQuestType<Boolean>() {

    // only for malls, big stores and rest areas because users should not need to go inside a non-public
    // place to solve the quest. (Considering malls and department stores public enough)
    override val elementFilter = """
        nodes, ways with
        (
          shop ~ mall|department_store
          or highway ~ services|rest_area
          or tourism ~ camp_site|caravan_site
        )
        and !toilets
    """
    override val changesetComment = "Survey toilet availabilities"
    override val wikiLink = "Key:toilets"
    override val icon = R.drawable.ic_quest_toilets
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_toiletAvailability_title

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags["toilets"] = answer.toYesNo()
    }
}
