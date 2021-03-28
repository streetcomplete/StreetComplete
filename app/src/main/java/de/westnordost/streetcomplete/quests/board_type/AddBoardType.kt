package de.westnordost.streetcomplete.quests.board_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType

class AddBoardType : OsmFilterQuestType<BoardType>() {

    override val elementFilter = """
        nodes with information = board
         and access !~ private|no
         and (!board_type or board_type ~ yes|board)
    """
    override val commitMessage = "Add board type"
    override val wikiLink = "Key:board_type"
    override val icon = R.drawable.ic_quest_board_type
    override val isDeleteElementEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_board_type_title

    override fun createForm() = AddBoardTypeForm()

    override fun applyAnswerTo(answer: BoardType, changes: StringMapChangesBuilder) {
        if(answer == BoardType.MAP) {
            changes.modify("information", "map")
        } else {
            changes.addOrModify("board_type", answer.osmValue)
        }
    }
}
