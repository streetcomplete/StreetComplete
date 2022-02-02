package de.westnordost.streetcomplete.quests.board_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.RARE

class AddBoardType : OsmFilterQuestType<BoardType>() {

    override val elementFilter = """
        nodes with
         tourism = information
         and information = board
         and access !~ private|no
         and (!board_type or board_type ~ yes|board)
    """
    override val changesetComment = "Add board type"
    override val wikiLink = "Key:board_type"
    override val icon = R.drawable.ic_quest_board_type
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = listOf(RARE, CITIZEN, OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_board_type_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with tourism = information and information = board")

    override fun createForm() = AddBoardTypeForm()

    override fun applyAnswerTo(answer: BoardType, tags: Tags, timestampEdited: Long) {
        if (answer == BoardType.MAP) {
            tags["information"] = "map"
        } else {
            tags["board_type"] = answer.osmValue
        }
    }
}
