package de.westnordost.streetcomplete.quests.max_height

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker

class AddMaxPhysicalHeight(
    private val checkArSupport: ArSupportChecker
) : OsmElementQuestType<MaxPhysicalHeightAnswer> {

    private val nodeFilter by lazy { """
        nodes with (
          barrier = height_restrictor
          or amenity = parking_entrance and parking ~ underground|multi-storey
        )
        and (maxheight = below_default or source:maxheight ~ ".*estimat.*")
        and !maxheight:physical
        and access !~ private|no
        and vehicle !~ private|no
    """.toElementFilterExpression() }

    private val wayFilter by lazy { """
        ways with
        highway ~ ${ALL_ROADS.joinToString("|")}
        and (maxheight = below_default or source:maxheight ~ ".*estimat.*")
        and !maxheight:physical
        and access !~ private|no
        and vehicle !~ private|no
    """.toElementFilterExpression() }

    override val changesetComment = "Specify maximum physical heights"
    override val wikiLink = "Key:maxheight"
    override val icon = R.drawable.ic_quest_max_height_measure
    override val achievements = listOf(EditTypeAchievement.CAR)
    override val defaultDisabledMessage: Int
        get() = if (!checkArSupport()) R.string.default_disabled_msg_no_ar else 0

    override fun getTitle(tags: Map<String, String>): Int {
        val isBelowBridge = tags["amenity"] != "parking_entrance"
            && tags["barrier"] != "height_restrictor"
            && tags["tunnel"] == null
            && tags["covered"] == null
            && tags["man_made"] != "pipeline"
        // only the "below the bridge" situation may need some context
        return when {
            isBelowBridge -> R.string.quest_maxheight_below_bridge_title
            else          -> R.string.quest_maxheight_title
        }
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.nodes.filter { nodeFilter.matches(it) } + mapData.ways.filter { wayFilter.matches(it) }

    override fun isApplicableTo(element: Element): Boolean =
        nodeFilter.matches(element) || wayFilter.matches(element)

    override fun createForm() = AddMaxPhysicalHeightForm()

    override fun applyAnswerTo(answer: MaxPhysicalHeightAnswer, tags: Tags, timestampEdited: Long) {
        // overwrite maxheight value but retain the info that there is no sign onto another tag
        tags["maxheight"] = answer.height.toOsmValue()
        tags["maxheight:signed"] = "no"

        if (answer.isARMeasurement) {
            tags["source:maxheight"] = "ARCore"
        } else {
            tags.remove("source:maxheight")
        }
    }
}
