package de.westnordost.streetcomplete.quests.summit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
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

    override val changesetComment = "Specify whether summit registers are present"
    override val wikiLink = "Key:summit:register"
    override val icon = R.drawable.ic_quest_peak
    override val achievements = listOf(RARE, OUTDOORS)
    override val enabledInCountries = NoCountriesExcept(
        // regions gathered in
        // https://github.com/streetcomplete/StreetComplete/issues/561#issuecomment-325623974

        // Europe
        "AT", // https://de.wikipedia.org/wiki/Gipfelkreuz
        "CH", // https://de.wikipedia.org/wiki/Gipfelkreuz
        "CZ", // https://cs.wikipedia.org/wiki/Vrcholov%C3%A1_kniha
        "DE", // https://de.wikipedia.org/wiki/Gipfelbuch
        "ES", // https://es.wikipedia.org/wiki/Comprobante_de_cumbre
        "FR", // https://it.wikipedia.org/wiki/Libro_di_vetta#Alcuni_esempi_di_libri_di_vetta
        "GR", // https://it.wikipedia.org/wiki/Libro_di_vetta#Alcuni_esempi_di_libri_di_vetta
        "IT", // https://it.wikipedia.org/wiki/Libro_di_vetta
        // not "NL": https://nl.wikipedia.org/wiki/Gipfelbuch is about "foreign" summit registers e.g. in the Alps
        // not "PL": https://github.com/westnordost/StreetComplete/issues/561#issuecomment-325504455
        "RO", // https://es.wikipedia.org/wiki/Cruz_de_la_cumbre#Ejemplos
        "SI", // https://it.wikipedia.org/wiki/Libro_di_vetta#Alcuni_esempi_di_libri_di_vetta
        "SK", // https://it.wikipedia.org/wiki/Croce_di_vetta#Alcuni_esempi_di_croci_di_vetta

        // Americas
        "AR", // https://en.wikipedia.org/wiki/Summit_cross#Gallery
        "PE", // https://es.wikipedia.org/wiki/Cruz_de_la_cumbre#Ejemplos
        "US", // https://de.wikipedia.org/wiki/Gipfelkreuz
    )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_summit_register_title2

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val peaks = mapData.nodes.filter { filter.matches(it) }
        if (peaks.isEmpty()) return emptyList()

        val hikingPathsAndRoutes = getHikingPathsAndRoutes(mapData)

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

    override fun isApplicableTo(element: Element) = when {
        !filter.matches(element) -> false
        element.tags["summit:cross"] == "yes" || element.tags.containsKey("summit:register") -> true
        else -> null
    }

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("summit:register", answer.toYesNo())
    }
}
