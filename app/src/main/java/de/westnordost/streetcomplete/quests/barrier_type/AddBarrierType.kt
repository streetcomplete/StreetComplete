package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder

class AddBarrierType : OsmFilterQuestType<BarrierType>() {

    override val elementFilter = """
        nodes with barrier=yes
         and !man_made
         and !historic
         and !military
         and !power
         and !tourism
         and !attraction
         and !amenity
         and !leisure
    """
    override val commitMessage = "Add specific barrier type on a point"
    override val wikiLink = "Key:barrier"
    override val icon = R.drawable.ic_quest_barrier
    override val isDeleteElementEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_barrier_type_title

    override fun createForm() = AddBarrierTypeForm()

    override fun applyAnswerTo(answer: BarrierType, changes: StringMapChangesBuilder) {
        changes.modify("barrier", answer.osmValue)
        when (answer) {
            BarrierType.STILE_SQUEEZER -> {
                changes.addOrModify("stile", "squeezer")
            }
            BarrierType.STILE_LADDER -> {
                changes.addOrModify("stile", "ladder")
            }
            BarrierType.STILE_STEPOVER -> {
                changes.addOrModify("stile", "stepover")
            }
        }

    }
}
