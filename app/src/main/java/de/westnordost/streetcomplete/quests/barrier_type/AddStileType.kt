package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.hasCheckDate
import de.westnordost.streetcomplete.osm.updateCheckDate

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

    override val changesetComment = "Specify stile types"
    override val wikiLink = "Key:stile"
    override val icon = R.drawable.ic_quest_no_cow
    override val isDeleteElementEnabled = true
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_stile_type_title

    override fun createForm() = AddStileTypeForm()

    override fun applyAnswerTo(answer: StileTypeAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is StileType -> {
                val newType = answer.osmValue
                val newMaterial = answer.osmMaterialValue
                val oldType = tags["stile"]
                val oldMaterial = tags["material"]
                val stileWasRebuilt =
                    oldType != null && oldType != newType
                    || newMaterial != null && oldMaterial != null && oldMaterial != newMaterial

                // => properties that refer to the old replaced stile should be removed
                if (stileWasRebuilt) {
                    STILE_PROPERTIES.forEach { tags.remove(it) }
                }
                if (newMaterial != null) {
                    tags["material"] = newMaterial
                }
                tags["stile"] = newType
            }
            is ConvertedStile -> {
                STILE_PROPERTIES.forEach { tags.remove(it) }
                tags.remove("stile")
                tags["barrier"] = answer.newBarrier
            }
        }
        // policy is to not remove a check date if one is already there but update it instead
        if (!tags.hasChanges || tags.hasCheckDate()) {
            tags.updateCheckDate()
        }
    }

    companion object {
        private val STILE_PROPERTIES = listOf(
            "step_count", "wheelchair", "bicycle",
            "dog_gate", "material", "height", "width", "stroller", "steps"
        )
    }
}
