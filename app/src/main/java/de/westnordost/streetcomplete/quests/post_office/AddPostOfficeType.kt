package de.westnordost.streetcomplete.quests.post_office

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags

class AddPostOfficeType : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
          amenity = post_office
          and !post_office
    """
    override val changesetComment = "Add post office"
    override val defaultDisabledMessage = R.string.default_disabled_msg_ee
    override val wikiLink = "Key:post_office"
    override val icon = R.drawable.ic_quest_post_office
    override val isReplacePlaceEnabled = true
    override val achievements = listOf(EditTypeAchievement.CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_postOffice_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = post_office or post_office")

    override fun createForm() = AddPostOfficeTypeForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["post_office"] = answer
    }
}
