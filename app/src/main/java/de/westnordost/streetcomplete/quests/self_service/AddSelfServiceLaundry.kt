package de.westnordost.streetcomplete.quests.self_service

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddSelfServiceLaundry : OsmFilterQuestType<Boolean>() {

    override val elementFilter = "nodes, ways with shop = laundry and !self_service"
    override val commitMessage = "Add self service info"
    override val wikiLink = "Tag:shop=laundry"
    override val icon = R.drawable.ic_quest_laundry

    override fun getTitle(tags: Map<String, String>) = R.string.quest_laundrySelfService_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("self_service", answer.toYesNo())
    }
}
