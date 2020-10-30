package de.westnordost.streetcomplete.quests.recycling_glass

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.quests.recycling_glass.RecyclingGlass.*

class DetermineRecyclingGlass : OsmFilterQuestType<RecyclingGlass>() {

    override val elementFilter = """
        nodes with amenity = recycling and recycling_type = container
         and recycling:glass = yes and !recycling:glass_bottles
    """
    override val commitMessage = "Determine whether any glass or just glass bottles can be recycled here"
    override val wikiLink = "Key:recycling"
    override val icon = R.drawable.ic_quest_recycling_glass
    // see isUsuallyAnyGlassRecycleableInContainers.yml
    override val enabledInCountries = AllCountriesExcept(
        "CZ"
    )

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
