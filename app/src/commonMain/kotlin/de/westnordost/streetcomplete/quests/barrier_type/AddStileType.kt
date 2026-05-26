package de.westnordost.streetcomplete.quests.barrier_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.hasCheckDate
import de.westnordost.streetcomplete.osm.updateCheckDate
import de.westnordost.streetcomplete.quests.barrier_type.StileTypeAnswer.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
        val excludedWayNodeIds = mapData.ways
            .filter { excludedWaysFilter.matches(it) }
            .flatMapTo(HashSet()) { it.nodeIds }

        return mapData.nodes
            .filter { stileNodeFilter.matches(it) && it.id !in excludedWayNodeIds }
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!stileNodeFilter.matches(element)) false else null

    override val changesetComment = "Specify stile types"
    override val wikiLink = "Key:stile"
    override val icon = Res.drawable.quest_no_cow
    override val title = Res.string.quest_stile_type_title
    override val achievements = listOf(OUTDOORS)

    @Composable
    override fun Form(onAnswer: (StileTypeAnswer) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = StileTypeAnswer.entries,
            itemsPerRow = 2,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onClickOk = onAnswer,
        )
    }

    override fun applyAnswerTo(answer: StileTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            SQUEEZER, LADDER, STEPOVER_WOODEN, STEPOVER_STONE -> {
                val newType = when (answer) {
                    SQUEEZER -> "squeezer"
                    LADDER -> "ladder"
                    STEPOVER_WOODEN -> "stepover"
                    STEPOVER_STONE -> "stepover"
                }
                val newMaterial = when (answer) {
                    STEPOVER_STONE -> "stone"
                    STEPOVER_WOODEN -> "wood"
                    else -> null
                }
                val oldType = tags["stile"]
                val oldMaterial = tags["material"]
                val stileWasRebuilt =
                    oldType != null && oldType != newType ||
                    newMaterial != null && oldMaterial != null && oldMaterial != newMaterial

                // => properties that refer to the old replaced stile should be removed
                if (stileWasRebuilt) {
                    STILE_PROPERTIES.forEach { tags.remove(it) }
                }
                if (newMaterial != null) {
                    tags["material"] = newMaterial
                }
                tags["stile"] = newType
            }
            KISSING_GATE, PASSAGE, GATE -> {
                tags["barrier"] = when (answer) {
                    KISSING_GATE -> "kissing_gate"
                    PASSAGE -> "entrance"
                    GATE -> "gate"
                }
                tags.remove("stile")
                STILE_PROPERTIES.forEach { tags.remove(it) }
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
