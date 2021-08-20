package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType

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
            BarrierType.STILE_STEPOVER_WOODEN -> {
                changes.addOrModify("stile", "stepover")
                changes.addOrModify("material", "wood")
            }
            BarrierType.STILE_STEPOVER_STONE -> {
                changes.addOrModify("stile", "stepover")
                changes.addOrModify("material", "stone")
            }
        }

    }
}
