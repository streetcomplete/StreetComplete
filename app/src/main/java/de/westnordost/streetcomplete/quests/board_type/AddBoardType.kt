package de.westnordost.streetcomplete.quests.board_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddBoardType(o: OverpassMapDataDao) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = "nodes with information=board and access !~ private|no and !board_type"
    override val commitMessage = "Add board type"
    override val icon = R.drawable.ic_quest_apple

    override fun getTitle(tags: Map<String, String>) = R.string.quest_board_type_title

    override fun createForm() = AddBoardTypeForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("board_type", answer)
    }
}
