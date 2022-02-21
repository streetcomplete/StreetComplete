package de.westnordost.streetcomplete.quests.powerpoles_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BUILDING

class AddPowerPolesMaterial : OsmFilterQuestType<PowerPolesMaterial>() {

    override val elementFilter = """
        nodes with
          (power = pole or man_made = utility_pole)
          and !material
    """
    override val changesetComment = "Add power poles material type"
    override val wikiLink = "Tag:power=pole"
    override val icon = R.drawable.ic_quest_power

    override val questTypeAchievements = listOf(BUILDING)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_powerPolesMaterial_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry): Sequence<Element> {
        val mapData = getMapData()
        // and also show the (power) lines themselves
        return mapData.filter("nodes with power = pole or man_made = utility_pole") +
            mapData.filter("ways with power ~ line|minor_line or communication = line or telecom = line")
    }

    // map data density is usually lower where there are power poles and more context is necessary
    // when looking at them from afar
    override val highlightedElementsRadius get() = 100.0

    override fun createForm() = AddPowerPolesMaterialForm()

    override fun applyAnswerTo(answer: PowerPolesMaterial, tags: Tags, timestampEdited: Long) {
        tags["material"] = answer.osmValue
    }
}
