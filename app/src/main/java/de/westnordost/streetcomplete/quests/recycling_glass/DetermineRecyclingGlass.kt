package de.westnordost.streetcomplete.quests.recycling_glass

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.recycling_glass.RecyclingGlass.ANY
import de.westnordost.streetcomplete.quests.recycling_glass.RecyclingGlass.BOTTLES

class DetermineRecyclingGlass : OsmFilterQuestType<RecyclingGlass>() {

    override val elementFilter = """
        nodes, ways with
          amenity = recycling
          and recycling_type = container
          and recycling:glass = yes
          and !recycling:glass_bottles
          and access !~ private|no
    """
    override val changesetComment = "Determine whether any glass or just glass bottles can be recycled here"
    override val wikiLink = "Key:recycling"
    override val icon = R.drawable.ic_quest_recycling_glass
    // see isUsuallyAnyGlassRecycleableInContainers.yml
    override val enabledInCountries = AllCountriesExcept("CZ")
    override val isDeleteElementEnabled = true
    override val achievements = listOf(CITIZEN)

    override val hint = R.string.quest_determineRecyclingGlass_description_any_glass

    override fun getTitle(tags: Map<String, String>) = R.string.quest_recycling_glass_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = recycling")

    override fun createForm() = DetermineRecyclingGlassForm()

    override fun applyAnswerTo(answer: RecyclingGlass, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            ANY -> {
                // to mark that it has been checked
                tags["recycling:glass_bottles"] = "yes"
            }
            BOTTLES -> {
                tags["recycling:glass_bottles"] = "yes"
                tags["recycling:glass"] = "no"
            }
        }
    }
}
