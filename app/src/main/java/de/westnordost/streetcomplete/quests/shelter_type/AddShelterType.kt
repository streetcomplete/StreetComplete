package de.westnordost.streetcomplete.quests.shelter_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags

class AddShelterType : OsmFilterQuestType<ShelterType>() {

    override val elementFilter = """
        nodes, ways with
          amenity = shelter
          and !shelter_type
    """
    override val changesetComment = "Specify shelter types"
    override val wikiLink = "Key:shelter_type"
    override val icon = R.drawable.ic_quest_shelter_type
    override val isDeleteElementEnabled = true
    override val achievements = listOf(EditTypeAchievement.OUTDOORS)
    override val defaultDisabledMessage: Int = R.string.quest_shelter_type_disabled_msg

    override fun getTitle(tags: Map<String, String>) = R.string.quest_shelter_type_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = shelter")

    override fun createForm() = AddShelterTypeForm()

    override fun applyAnswerTo(answer: ShelterType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["shelter_type"] = answer.osmValue
    }
}
