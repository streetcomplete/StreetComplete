package de.westnordost.streetcomplete.quests.orchard_produce

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddOrchardProduce(o: OverpassMapDataDao) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = "ways, relations with landuse = orchard and !trees and !produce and !crop"
    override val commitMessage = "Add orchard produces"
    override val icon = R.drawable.ic_quest_apple

    override fun getTitle(tags: Map<String, String>) = R.string.quest_orchard_produce_title

    override fun createForm() = AddOrchardProduceForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("produce", answer)

        when(answer) {
            "grape" -> changes.modify("landuse", "vineyard")
            "sisal" -> changes.modify("landuse", "farmland")
        }
    }
}
