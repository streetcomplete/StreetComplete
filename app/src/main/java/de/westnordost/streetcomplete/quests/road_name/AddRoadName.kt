package de.westnordost.streetcomplete.quests.road_name

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.POSTMAN
import de.westnordost.streetcomplete.quests.LocalizedName

class AddRoadName : OsmElementQuestType<RoadNameAnswer> {

    private val filter by lazy { """
        ways with
          highway ~ primary|secondary|tertiary|unclassified|residential|living_street|pedestrian
          and !name and !name:left and !name:right
          and !ref
          and noname != yes
          and !junction
          and area != yes
          and (
            access !~ private|no
            or foot and foot !~ private|no
          )
    """.toElementFilterExpression() }

    override val enabledInCountries = AllCountriesExcept("JP")
    override val commitMessage = "Determine road names and types"
    override val wikiLink = "Key:name"
    override val icon = R.drawable.ic_quest_street_name
    override val hasMarkersAtEnds = true
    override val isSplitWayEnabled = true

    override val questTypeAchievements = listOf(CAR, PEDESTRIAN, POSTMAN)

    override fun getTitle(tags: Map<String, String>) =
        if (tags["highway"] == "pedestrian")
            R.string.quest_streetName_pedestrian_title
        else
            R.string.quest_streetName_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        return mapData.ways.filter { filter.matches(it) }
    }

    override fun isApplicableTo(element: Element): Boolean = filter.matches(element)

    override fun createForm() = AddRoadNameForm()

    override fun applyAnswerTo(answer: RoadNameAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is NoRoadName        -> changes.add("noname", "yes")
            is RoadIsServiceRoad -> {
                // The understanding of what is a service road is much broader in common language
                // than what the highway=service tagging covers. For example, certain traffic-calmed
                // driveways / service roads may be tagged as highway=living_street. We do not want
                // to overwrite this, so let's keep it a living street in that case (see #2431)
                if (changes.getPreviousValue("highway") == "living_street") {
                    changes.add("noname", "yes")
                } else {
                    changes.modify("highway", "service")
                }
            }
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
    }
}

private fun LocalizedName.isRef() =
    languageTag.isEmpty() && name.matches("[A-Z]{0,3}[ -]?[0-9]{0,5}".toRegex())
