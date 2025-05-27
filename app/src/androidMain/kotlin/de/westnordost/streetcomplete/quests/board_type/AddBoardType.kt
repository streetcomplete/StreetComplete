package de.westnordost.streetcomplete.quests.board_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.osm.Tags

class AddBoardType : OsmFilterQuestType<BoardTypeAnswer>() {

    override val elementFilter = """
        nodes with
         tourism = information
         and information = board
         and access !~ private|no
         and (!board_type or board_type ~ yes|board)
    """
    override val changesetComment = "Specify board types"
    override val wikiLink = "Key:board_type"
    override val icon = R.drawable.ic_quest_board_type
    override val isDeleteElementEnabled = true
    override val achievements = listOf(RARE, CITIZEN, OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_board_type_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with tourism = information and information = board")

    override fun createForm() = AddBoardTypeForm()

    override fun applyAnswerTo(answer: BoardTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer is NoBoardJustMap) {
            tags["information"] = "map"
        } else if (answer is BoardType) {
            tags["board_type"] = answer.osmValue
        }
    }
}
