package de.westnordost.streetcomplete.quests.memorial_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement
import de.westnordost.streetcomplete.quests.barrier_type.applyTo

class AddMemorialType : OsmFilterQuestType<MemorialTypeAnswer>() {

    override val elementFilter = """
        nodes, ways, relations with
          historic=memorial
          and (!memorial or memorial=yes)
          and !memorial:type
    """
    override val changesetComment = "Add memorial type"
    override val wikiLink = "Key:memorial"
    override val icon = R.drawable.ic_quest_memorial
    override val questTypeAchievements = listOf(QuestTypeAchievement.CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_memorialType_title

    override fun createForm() = AddMemorialTypeForm()

    override fun applyAnswerTo(answer: MemorialTypeAnswer, tags: Tags, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}
