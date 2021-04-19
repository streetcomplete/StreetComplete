package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder

class AddStileType : OsmFilterQuestType<BarrierType>() {

    override val elementFilter = """
        nodes with barrier=stile and !stile
    """
    override val commitMessage = "Add specific stile type"
    override val wikiLink = "Key:stile"
    override val icon = R.drawable.ic_quest_cow
    override val isDeleteElementEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_stile_type_title

    override fun createForm() = AddStileTypeForm()

    override fun applyAnswerTo(answer: BarrierType, changes: StringMapChangesBuilder) {
        when (answer) {
            BarrierType.STILE_SQUEEZER -> {
                changes.add("stile", "squeezer")
            }
            BarrierType.STILE_LADDER -> {
                changes.add("stile", "ladder")
            }
            BarrierType.STILE_STEPOVER -> {
                changes.add("stile", "stepover")
            }
            BarrierType.KISSING_GATE -> {
                changes.modify("barrier", "kissing_gate")
                changes.deleteIfExists("step_count")
                changes.deleteIfExists("wheelchair")
                changes.deleteIfExists("bicycle")
                changes.deleteIfExists("dog_gate")
                changes.deleteIfExists("material")
                changes.deleteIfExists("height")
                changes.deleteIfExists("width")
                changes.deleteIfExists("stroller")
            }
        }

    }
}
