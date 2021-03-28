package de.westnordost.streetcomplete.quests.summit_register

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import de.westnordost.streetcomplete.util.distanceToArcs

class AddSummitRegister : OsmElementQuestType<Boolean> {

    private val filter by lazy { """
        nodes with 
          natural = peak and name and 
          (!summit:register or summit:register older today -4 years)
    """.toElementFilterExpression() }

    override val commitMessage = "Add whether summit register is present"
    override val wikiLink = "Key:summit:register"
    override val icon = R.drawable.ic_quest_peak

    override val enabledInCountries = NoCountriesExcept(
        // regions gathered in
        // https://github.com/westnordost/StreetComplete/issues/561#issuecomment-325623974

        // Europe
        "AT", "DE", "CZ", "ES", "IT", "FR", "GR", "SI", "CH", "RO", "SK",

        //Americas
        "US", "AR", "PE"
    )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_summit_register_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val peaks = mapData.nodes.filter { filter.matches(it) }
        if (peaks.isEmpty()) return emptyList()

        val hikingRoutes = mapData.relations
            .filter { it.tags?.get("route") == "hiking" }
            .mapNotNull { mapData.getRelationGeometry(it.id) as? ElementPolylinesGeometry }
        if (hikingRoutes.isEmpty()) return emptyList()

        // yes, this is very inefficient, however, peaks are very rare
        return peaks.filter { peak ->
            hikingRoutes.any { hikingRoute ->
                hikingRoute.polylines.any { ways ->
                    peak.position.distanceToArcs(ways) <= 10
                }
            }
        }
    }

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("summit:register", answer.toYesNo())
    }
}
