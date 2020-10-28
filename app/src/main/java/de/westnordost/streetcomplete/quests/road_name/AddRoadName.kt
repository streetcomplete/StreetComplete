package de.westnordost.streetcomplete.quests.road_name

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.quests.LocalizedName
import de.westnordost.streetcomplete.quests.road_name.data.RoadNameSuggestionEntry
import de.westnordost.streetcomplete.quests.road_name.data.RoadNameSuggestionsDao
import de.westnordost.streetcomplete.quests.road_name.data.toRoadNameByLanguage

class AddRoadName(
    private val roadNameSuggestionsDao: RoadNameSuggestionsDao
) : OsmElementQuestType<RoadNameAnswer> {

    private val filter by lazy { """
        ways with 
          highway ~ primary|secondary|tertiary|unclassified|residential|living_street|pedestrian
          and !name
          and !ref
          and noname != yes
          and !junction
          and area != yes
          and (
            access !~ private|no
            or foot and foot !~ private|no
          )
    """.toElementFilterExpression() }

    private val roadsWithNamesFilter by lazy { """
        ways with
          highway ~ ${ALL_ROADS.joinToString("|")}
          and name
    """.toElementFilterExpression() }

    override val enabledInCountries = AllCountriesExcept("JP")
    override val commitMessage = "Determine road names and types"
    override val wikiLink = "Key:name"
    override val icon = R.drawable.ic_quest_street_name
    override val hasMarkersAtEnds = true
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>) =
        if (tags["highway"] == "pedestrian")
            R.string.quest_streetName_pedestrian_title
        else
            R.string.quest_streetName_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val roadsWithoutNames = mapData.ways.filter { filter.matches(it) }

        if (roadsWithoutNames.isNotEmpty()) {
            val roadsWithNames = mapData.ways
                .filter { roadsWithNamesFilter.matches(it) }
                .mapNotNull {
                    val geometry = mapData.getWayGeometry(it.id) as? ElementPolylinesGeometry
                    val roadNamesByLanguage = it.tags?.toRoadNameByLanguage()
                    if (geometry != null && roadNamesByLanguage != null) {
                        RoadNameSuggestionEntry(it.id, roadNamesByLanguage, geometry.polylines.first())
                    } else null
                }
            roadNameSuggestionsDao.putRoads(roadsWithNames)
        }
        return roadsWithoutNames
    }

    override fun isApplicableTo(element: Element) = filter.matches(element)

    override fun createForm() = AddRoadNameForm()

    override fun applyAnswerTo(answer: RoadNameAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is NoRoadName        -> changes.add("noname", "yes")
            is RoadIsServiceRoad -> changes.modify("highway", "service")
            is RoadIsTrack       -> changes.modify("highway", "track")
            is RoadIsLinkRoad    -> {
                val prevValue = changes.getPreviousValue("highway")
                if (prevValue?.matches("primary|secondary|tertiary".toRegex()) == true) {
                    changes.modify("highway", prevValue + "_link")
                }
            }
            is RoadName -> {
                val singleName = answer.localizedNames.singleOrNull()
                if (singleName?.isRef() == true) {
                    changes.add("ref", singleName.name)
                } else {
                    applyAnswerRoadName(answer, changes)
                }
            }
        }
    }

    private fun applyAnswerRoadName(answer: RoadName, changes: StringMapChangesBuilder) {
        for ((languageTag, name) in answer.localizedNames) {
            val key = when (languageTag) {
                "" -> "name"
                "international" -> "int_name"
                else -> "name:$languageTag"
            }
            changes.addOrModify(key, name)
        }
        // these params are passed from the form only to update the road name suggestions so that
        // newly input street names turn up in the suggestions as well
        val roadNameByLanguage = answer.localizedNames.associate { it.languageTag to it.name }
        roadNameSuggestionsDao.putRoad( answer.wayId, roadNameByLanguage, answer.wayGeometry)
    }

    override fun cleanMetadata() {
        roadNameSuggestionsDao.cleanUp()
    }
}

private fun LocalizedName.isRef() =
    languageTag.isEmpty() && name.matches("[A-Z]{0,3}[ -]?[0-9]{0,5}".toRegex())
