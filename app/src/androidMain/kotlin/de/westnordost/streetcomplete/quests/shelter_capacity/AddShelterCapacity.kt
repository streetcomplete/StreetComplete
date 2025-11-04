package de.westnordost.streetcomplete.quests.shelter_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddShelterCapacity : OsmFilterQuestType<Int>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
          (
            (
              amenity = shelter
              and shelter_type = basic_hut
            )
            or tourism = wilderness_hut
          )
          and !capacity
          and !capacity:persons
          and access !~ private|no
    """
    override val changesetComment = "Specify shelter capacities"
    override val wikiLink = "Tag:amenity=shelter"
    override val icon = R.drawable.quest_shelter_capacity
    override val isDeleteElementEnabled = true
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_shelter_capacity_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = shelter")

    override fun createForm() = AddShelterCapacityForm()

    override fun applyAnswerTo(answer: Int, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("capacity", answer.toString())
    }
}
