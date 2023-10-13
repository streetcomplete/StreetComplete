package de.westnordost.streetcomplete.quests.bbq_fuel

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags

class AddBbqFuel : OsmFilterQuestType<BbqFuel>() {

    override val elementFilter = """
        nodes, ways with
          amenity = bbq
          and !fuel
          and access !~ no|private
    """

    override val changesetComment = "Specify barbecue fuel"
    override val wikiLink = "Key:amenity=bbq"
    override val icon = R.drawable.ic_quest_fire
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bbq_fuel_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = bbq")

    override fun createForm() = AddBbqFuelForm()

    override fun applyAnswerTo(answer: BbqFuel, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["fuel"] = answer.osmValue
    }
}
