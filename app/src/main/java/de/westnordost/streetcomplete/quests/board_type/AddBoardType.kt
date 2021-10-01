package de.westnordost.streetcomplete.quests.board_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.RARE
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.OUTDOORS

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

    override val questTypeAchievements = listOf(RARE, CITIZEN, OUTDOORS)

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
