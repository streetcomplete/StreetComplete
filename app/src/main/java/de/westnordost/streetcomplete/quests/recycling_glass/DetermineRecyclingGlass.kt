package de.westnordost.streetcomplete.quests.recycling_glass

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.quests.recycling_glass.RecyclingGlass.*

class DetermineRecyclingGlass(overpassServer: OverpassMapDataAndGeometryDao) :
    SimpleOverpassQuestType<RecyclingGlass>(overpassServer) {

    override val tagFilters = """
        nodes with amenity = recycling and recycling_type = container
         and recycling:glass = yes and !recycling:glass_bottles
    """
    override val commitMessage = "Determine whether any glass or just glass bottles can be recycled here"
    override val icon = R.drawable.ic_quest_recycling_glass

    override fun getTitle(tags: Map<String, String>) = R.string.quest_recycling_glass_title

    override fun createForm() = DetermineRecyclingGlassForm()

    override fun applyAnswerTo(answer: RecyclingGlass, changes: StringMapChangesBuilder) {
        when(answer) {
            ANY -> {
                // to mark that it has been checked
                changes.add("recycling:glass_bottles", "yes")
            }
            BOTTLES -> {
                changes.add("recycling:glass_bottles", "yes")
                changes.modify("recycling:glass", "no")
            }
        }
    }
}
