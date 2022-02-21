package de.westnordost.streetcomplete.quests.orchard_produce

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.OUTDOORS

class AddOrchardProduce : OsmFilterQuestType<List<OrchardProduce>>() {

    override val elementFilter = """
        ways, relations with landuse = orchard
        and !trees and !produce and !crop
        and orchard != meadow_orchard
    """
    override val changesetComment = "Add orchard produces"
    override val wikiLink = "Tag:landuse=orchard"
    override val icon = R.drawable.ic_quest_apple

    override val questTypeAchievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_orchard_produce_title

    override fun createForm() = AddOrchardProduceForm()

    override fun applyAnswerTo(answer: List<OrchardProduce>, tags: Tags, timestampEdited: Long) {
        tags["produce"] = answer.joinToString(";") { it.osmValue }

        val landuse = answer.singleOrNull()?.osmLanduseValue
        if (landuse != null) {
            tags["landuse"] = landuse
        }
    }
}
