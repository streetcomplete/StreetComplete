package de.westnordost.streetcomplete.quests.fire_hydrant

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder

class AddFireHydrantType : OsmFilterQuestType<FireHydrantType>() {

    override val elementFilter = "nodes with emergency = fire_hydrant and !fire_hydrant:type"
    override val commitMessage = "Add fire hydrant type"
    override val wikiLink = "Tag:emergency=fire_hydrant"
    override val icon = R.drawable.ic_quest_fire_hydrant

    override fun getTitle(tags: Map<String, String>) = R.string.quest_fireHydrant_type_title

    override fun createForm() = AddFireHydrantTypeForm()

    override fun applyAnswerTo(answer: FireHydrantType, changes: StringMapChangesBuilder) {
        changes.add("fire_hydrant:type", answer.osmValue)
    }
}
