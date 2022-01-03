package de.westnordost.streetcomplete.quests.fire_hydrant

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.LIFESAVER

class AddFireHydrantPosition : OsmFilterQuestType<FireHydrantPosition>() {

    override val elementFilter = """
        nodes with
         emergency = fire_hydrant and
         (!fire_hydrant:position or fire_hydrant:position ~ "\?|fixme") and
         (fire_hydrant:type = pillar or fire_hydrant:type = underground)
    """
    override val commitMessage = "Add fire hydrant position"
    override val wikiLink = "Tag:emergency=fire_hydrant"
    override val icon = R.drawable.ic_quest_fire_hydrant_grass

    override val questTypeAchievements = listOf(LIFESAVER)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_fireHydrant_position_title

    override fun createForm() = AddFireHydrantPositionForm()

    override fun applyAnswerTo(answer: FireHydrantPosition, changes: StringMapChangesBuilder) {
        changes.add("fire_hydrant:position", answer.osmValue)
    }
}
