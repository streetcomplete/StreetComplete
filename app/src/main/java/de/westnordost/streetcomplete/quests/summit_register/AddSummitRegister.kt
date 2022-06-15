package de.westnordost.streetcomplete.quests.summit_register

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo
import de.westnordost.streetcomplete.util.math.distanceToArcs

class AddSummitRegister : OsmElementQuestType<Boolean> {

    private val filter by lazy { """
        nodes with
          natural = peak
          and name
          and (!summit:register or summit:register older today -4 years)
    """.toElementFilterExpression() }

    override val changesetComment = "Add whether summit register is present"
    override val wikiLink = "Key:summit:register"
    override val icon = R.drawable.ic_quest_peak
    override val achievements = listOf(RARE, OUTDOORS)
    override val enabledInCountries = NoCountriesExcept(
        // regions gathered in
        // https://github.com/streetcomplete/StreetComplete/issues/561#issuecomment-325623974

        // Europe
        "AT", "DE", "CZ", "ES", "IT", "FR", "GR", "SI", "CH", "RO", "SK",

        // Americas
        "US", "AR", "PE"
    )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_summit_register_title2

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val peaks = mapData.nodes.filter { filter.matches(it) }
        if (peaks.isEmpty()) return emptyList()

        val hikingPathsAndRoutes = mapData.ways.filter { hikingPathsFilter.matches(it) }
            .mapNotNull { mapData.getWayGeometry(it.id) as? ElementPolylinesGeometry } +
            mapData.relations.filter { it.tags["route"] == "hiking" }
                .mapNotNull { mapData.getRelationGeometry(it.id) as? ElementPolylinesGeometry }

        // yes, this is very inefficient, however, peaks are very rare
        return peaks.filter { peak ->
            peak.tags["summit:cross"] == "yes" || peak.tags.containsKey("summit:register") ||
            hikingPathsAndRoutes.any { hikingPath ->
                hikingPath.polylines.any { ways ->
                    peak.position.distanceToArcs(ways) <= 10
                }
            }
        }
    }

    private val hikingPathsFilter by lazy { """
        ways with
          highway = path
          and sac_scale ~ mountain_hiking|demanding_mountain_hiking|alpine_hiking|demanding_alpine_hiking|difficult_alpine_hiking
   """.toElementFilterExpression() }

    override fun isApplicableTo(element: Element): Boolean? =
        when {
            !filter.matches(element) -> false
            element.tags["summit:cross"] == "yes" || element.tags.containsKey("summit:register") -> true
            else -> null
        }

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("summit:register", answer.toYesNo())
    }
}
