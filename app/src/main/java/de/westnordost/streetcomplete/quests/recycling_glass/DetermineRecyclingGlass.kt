package de.westnordost.streetcomplete.quests.recycling_glass

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.quests.recycling_glass.RecyclingGlass.ANY
import de.westnordost.streetcomplete.quests.recycling_glass.RecyclingGlass.BOTTLES

class DetermineRecyclingGlass : OsmFilterQuestType<RecyclingGlass>() {

    override val elementFilter = """
        nodes with amenity = recycling and recycling_type = container
         and recycling:glass = yes and !recycling:glass_bottles
    """
    override val changesetComment = "Determine whether any glass or just glass bottles can be recycled here"
    override val wikiLink = "Key:recycling"
    override val icon = R.drawable.ic_quest_recycling_glass
    // see isUsuallyAnyGlassRecycleableInContainers.yml
    override val enabledInCountries = AllCountriesExcept("CZ")
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_recycling_glass_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = recycling")

    override fun createForm() = DetermineRecyclingGlassForm()

    override fun applyAnswerTo(answer: RecyclingGlass, tags: Tags, timestampEdited: Long) {
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
