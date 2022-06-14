package de.westnordost.streetcomplete.quests.summit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.RARE
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import de.westnordost.streetcomplete.util.ktx.toYesNo
import de.westnordost.streetcomplete.util.math.distanceToArcs

class AddSummitCross : OsmElementQuestType<Boolean> {

    private val filter by lazy { """
        nodes with
          natural = peak
          and name
          and (!summit:cross or summit:cross older today -16 years)
    """.toElementFilterExpression() }

    override val changesetComment = "Add whether summit cross is present"
    override val wikiLink = "Key:summit:cross"
    override val icon = R.drawable.ic_quest_summit_cross
    override val questTypeAchievements = listOf(RARE, OUTDOORS)
    override val enabledInCountries = COUNTRIES_WHERE_SUMMIT_MARKINGS_ARE_COMMON

    override fun getTitle(tags: Map<String, String>) = R.string.quest_summit_cross_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val peaks = mapData.nodes.filter { filter.matches(it) }
        if (peaks.isEmpty()) return emptyList()

        val hikingPathsAndRoutes = getHikingPathsAndRoutes(mapData)

        // yes, this is very inefficient, however, peaks are very rare
        return peaks.filter { peak ->
            peak.tags["summit:register"] == "yes" || peak.tags.containsKey("summit:cross") ||
            hikingPathsAndRoutes.any { hikingPath ->
                hikingPath.polylines.any { ways ->
                    peak.position.distanceToArcs(ways) <= 10
                }
            }
        }
    }

    override fun isApplicableTo(element: Element) = when {
        !filter.matches(element) -> false
        element.tags["summit:register"] == "yes" || element.tags.containsKey("summit:cross") -> true
        else -> null
    }

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("summit:cross", answer.toYesNo())
    }
}
