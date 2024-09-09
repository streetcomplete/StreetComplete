package de.westnordost.streetcomplete.quests.board_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.applyTo

class AddBoardName : OsmFilterQuestType<BoardNameAnswer>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
          tourism = information and information = board
          and board_type != notice
          and !board:title
        )
        and !name and noname != yes and name:signed != no
    """

    override val changesetComment = "Determine information board names"
    override val wikiLink = "Tag:information=board"
    override val icon = R.drawable.ic_quest_board_name
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_board_name_title

    override fun createForm() = AddBoardNameForm()

    override fun applyAnswerTo(answer: BoardNameAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NoBoardName -> {
                tags["noname"] = "yes"
            }
            is BoardName -> {
                answer.localizedNames.applyTo(tags)
            }
        }
    }
}
