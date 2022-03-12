package de.westnordost.streetcomplete.quests.max_height

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement
import de.westnordost.streetcomplete.measure.ArSupportChecker
import de.westnordost.streetcomplete.osm.ALL_ROADS

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

    override val changesetComment = "Add maximum heights"
    override val wikiLink = "Key:maxheight"
    override val icon = R.drawable.ic_quest_max_height_measure
    override val isSplitWayEnabled = true
    override val questTypeAchievements = listOf(QuestTypeAchievement.CAR)
    override val defaultDisabledMessage: Int
        get() = if (!checkArSupport()) R.string.default_disabled_msg_no_ar else 0

    override fun getTitle(tags: Map<String, String>): Int {
        val isParkingEntrance = tags["amenity"] == "parking_entrance"
        val isHeightRestrictor = tags["barrier"] == "height_restrictor"
        val isTunnel = tags["tunnel"] == "yes"
        val isBelowBridge =
            !isParkingEntrance && !isHeightRestrictor
                && tags["tunnel"] == null && tags["covered"] == null
                && tags["man_made"] != "pipeline"

        return when {
            isParkingEntrance  -> R.string.quest_maxheight_parking_entrance_title
            isHeightRestrictor -> R.string.quest_maxheight_height_restrictor_title
            isTunnel           -> R.string.quest_maxheight_tunnel_title
            isBelowBridge      -> R.string.quest_maxheight_below_bridge_title
            else               -> R.string.quest_maxheight_title
        }
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.nodes.filter { nodeFilter.matches(it) } + mapData.ways.filter { wayFilter.matches(it) }

    override fun isApplicableTo(element: Element): Boolean =
        nodeFilter.matches(element) || wayFilter.matches(element)

    override fun createForm() = AddHeightForm()

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
