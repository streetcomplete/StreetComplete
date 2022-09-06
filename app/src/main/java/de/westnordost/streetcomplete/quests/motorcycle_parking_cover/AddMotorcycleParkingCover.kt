package de.westnordost.streetcomplete.quests.motorcycle_parking_cover

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddMotorcycleParkingCover : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with amenity = motorcycle_parking
        and access !~ private|no
        and !covered
        and motorcycle_parking !~ shed|garage_boxes|building
    """
    override val changesetComment = "Specify motorcycle parkings covers"
    override val wikiLink = "Tag:amenity=motorcycle_parking"
    override val icon = R.drawable.ic_quest_motorcycle_parking_cover
    override val isDeleteElementEnabled = true
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_motorcycleParkingCoveredStatus_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = motorcycle_parking")

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags["covered"] = answer.toYesNo()
    }
}
