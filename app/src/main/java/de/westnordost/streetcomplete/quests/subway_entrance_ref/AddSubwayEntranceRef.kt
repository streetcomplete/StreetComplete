package de.westnordost.streetcomplete.quests.subway_entrance_ref

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags

class AddSubwayEntranceRef : OsmFilterQuestType<SubwayEntranceRefAnswer>() {
    override val elementFilter = """
        nodes with
          railway = subway_entrance
          and highway != elevator
          and !ref
          and ref:signed != no
    """
    override val changesetComment = "Specify subway entrance refs"
    override val wikiLink = "Tag:railway=subway_entrance"
    override val icon = R.drawable.ic_quest_subway_entrance_ref
    override val isDeleteElementEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_genericRef_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with railway = subway_entrance")
    override val highlightedElementsRadius: Double get() = 100.0

    override val defaultDisabledMessage: Int = R.string.quest_subwayEntranceRef_disabled_msg

    override fun createForm() = AddSubwayEntranceRefForm()

    override fun applyAnswerTo(answer: SubwayEntranceRefAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NoVisibleSubwayEntranceRef -> tags["ref:signed"] = "no"
            is SubwayEntranceRef ->          tags["ref"] = answer.ref
        }
    }
}
