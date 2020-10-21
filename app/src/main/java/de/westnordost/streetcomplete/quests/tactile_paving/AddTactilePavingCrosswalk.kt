package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.ElementFiltersParser
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.osm.osmquest.OsmMapDataQuestType
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddTactilePavingCrosswalk(private val r: ResurveyIntervalsStore) : OsmMapDataQuestType<Boolean> {

    private val crossingFilter by lazy { ElementFiltersParser().parse("""
        nodes with 
          (
            highway = traffic_signals and crossing = traffic_signals and foot != no
            or highway = crossing and foot != no
          )
          and (
            !tactile_paving
            or tactile_paving = no and tactile_paving older today -${r * 4} years
            or older today -${r * 8} years
          )
    """)}

    private val excludedWaysFilter by lazy { ElementFiltersParser().parse("""
        ways with 
          highway = cycleway and foot !~ yes|designated
          or highway and access ~ private|no
    """)}

    override val commitMessage = "Add tactile pavings on crosswalks"
    override val wikiLink = "Key:tactile_paving"
    override val icon = R.drawable.ic_quest_blind_pedestrian_crossing

    // See overview here: https://ent8r.github.io/blacklistr/?streetcomplete=tactile_paving/AddTactilePavingCrosswalk.kt
    // #750
    override val enabledInCountries = NoCountriesExcept(
            // Europe
            "NO", "SE",
            "GB", "IE", "NL", "BE", "FR", "ES",
            "DE", "PL", "CZ", "SK", "HU", "AT", "CH",
            "LV", "LT", "EE", "RU",
            // America
            "US", "CA", "AR",
            // Asia
            "HK", "SG", "KR", "JP",
            // Oceania
            "AU", "NZ"
    )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tactilePaving_title_crosswalk

    override fun getApplicableElements(mapData: MapDataWithGeometry): List<Element> {
        val excludedWayNodeIds = mutableSetOf<Long>()
        mapData.ways
            .filter { excludedWaysFilter.matches(it) }
            .flatMapTo(excludedWayNodeIds) { it.nodeIds }

        return mapData.nodes
            .filter { crossingFilter.matches(it) && it.id !in excludedWayNodeIds }
    }

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun createForm() = TactilePavingForm()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("tactile_paving", answer.toYesNo())
    }
}
