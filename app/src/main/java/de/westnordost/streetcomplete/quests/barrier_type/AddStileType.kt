package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.hasCheckDate
import de.westnordost.streetcomplete.data.meta.updateCheckDate
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.deleteIfExistList
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.OUTDOORS

class AddStileType : OsmElementQuestType<StileTypeAnswer> {

    private val stileNodeFilter by lazy { """
        nodes with
         barrier = stile
         and (!stile or older today -8 years)
    """.toElementFilterExpression() }

    private val excludedWaysFilter by lazy { """
        ways with
          access ~ private|no
          and foot !~ permissive|yes|designated
    """.toElementFilterExpression() }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val excludedWayNodeIds = mutableSetOf<Long>()
        mapData.ways
            .filter { excludedWaysFilter.matches(it) }
            .flatMapTo(excludedWayNodeIds) { it.nodeIds }

        return mapData.nodes
            .filter { stileNodeFilter.matches(it) && it.id !in excludedWayNodeIds }
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!stileNodeFilter.matches(element)) false else null

    override val commitMessage = "Add specific stile type"
    override val wikiLink = "Key:stile"
    override val icon = R.drawable.ic_quest_cow
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_stile_type_title

    override fun createForm() = AddStileTypeForm()

    override fun applyAnswerTo(answer: StileTypeAnswer, changes: StringMapChangesBuilder) {
        when (answer) {
            is StileType -> {
                val newType = answer.osmValue
                val newMaterial = answer.osmMaterialValue
                val oldType = changes.getPreviousValue("stile")
                val oldMaterial = changes.getPreviousValue("material")
                val stileWasRebuilt =
                    oldType != null && oldType != newType ||
                    newMaterial != null && oldMaterial != null && oldMaterial != newMaterial

                if (stileWasRebuilt) {
                    // => properties that refer to the old replaced stile should be removed
                    changes.deleteIfExistList(STILE_PROPERTIES - "material")
                    if(newMaterial != null) {
                        changes.addOrModify("material", newMaterial)
                    } else {
                        changes.deleteIfExists("material")
                    }
                } else if (newMaterial != null && oldMaterial == null) {
                    // not considered as rebuilt, but material info still
                    // can be added where it was missing
                    changes.add("material", newMaterial)
                }
                if (newType != oldType) {
                    changes.addOrModify("stile", newType)
                }
            }
            is ConvertedStile -> {
                changes.deleteIfExistList(STILE_PROPERTIES)
                changes.deleteIfExists("stile")
                changes.modify("barrier", answer.newBarrier)
            }
        }
        // policy is to not remove a check date if one is already there but update it instead
        if (changes.getChanges().isEmpty() || changes.hasCheckDate()) {
            changes.updateCheckDate()
        }
    }

    companion object {
        private val STILE_PROPERTIES = listOf(
            "step_count", "wheelchair", "bicycle",
            "dog_gate", "material", "height", "width", "stroller", "steps"
        )
    }
}
