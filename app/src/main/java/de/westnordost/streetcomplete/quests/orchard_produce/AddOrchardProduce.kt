package de.westnordost.streetcomplete.quests.orchard_produce

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder

class AddOrchardProduce : OsmFilterQuestType<List<String>>() {

    override val elementFilter = """
        ways, relations with landuse = orchard
        and !trees and !produce and !crop
        and orchard != meadow_orchard
    """
    override val commitMessage = "Add orchard produces"
    override val wikiLink = "Tag:landuse=orchard"
    override val icon = R.drawable.ic_quest_apple

    override fun getTitle(tags: Map<String, String>) = R.string.quest_orchard_produce_title

    override fun createForm() = AddOrchardProduceForm()

    override fun applyAnswerTo(answer: List<String>, changes: StringMapChangesBuilder) {
        changes.add("produce", answer.joinToString(";"))

        when(answer.singleOrNull()) {
            "grape" -> changes.modify("landuse", "vineyard")
            "sisal" -> changes.modify("landuse", "farmland")
        }
    }
}
