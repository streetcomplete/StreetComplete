package de.westnordost.streetcomplete.quests.board_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.localized_name.applyTo
import de.westnordost.streetcomplete.resources.*

class AddBoardName : OsmFilterQuestType<BoardNameAnswer>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways, relations with
        (
          tourism = information and information = board
          and board_type !~ notice|public_transport
          and !board:title
        )
        and !name and noname != yes and name:signed != no
    """

    override val changesetComment = "Determine information board names"
    override val wikiLink = "Tag:information=board"
    override val icon = R.drawable.quest_label_thing
    override val title = Res.string.quest_board_name_title
    override val achievements = listOf(OUTDOORS)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways, relations with tourism = information and information = board")

    override fun createForm() = AddBoardNameForm()

    override fun applyAnswerTo(answer: BoardNameAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is BoardNameAnswer.NoName -> {
                tags["noname"] = "yes"
            }
            is BoardName -> {
                answer.localizedNames.applyTo(tags)
            }
        }
    }
}
