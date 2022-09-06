package de.westnordost.streetcomplete.quests.memorial_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags

class AddMemorialType : OsmFilterQuestType<MemorialType>() {

    override val elementFilter = """
        nodes, ways, relations with
          historic=memorial
          and (!memorial or memorial=yes)
          and !memorial:type
    """
    override val changesetComment = "Specify memorial types"
    override val wikiLink = "Key:memorial"
    override val icon = R.drawable.ic_quest_memorial
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_memorialType_title

    override fun createForm() = AddMemorialTypeForm()

    override fun applyAnswerTo(answer: MemorialType, tags: Tags, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}
