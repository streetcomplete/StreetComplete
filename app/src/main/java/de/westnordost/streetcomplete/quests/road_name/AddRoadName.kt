package de.westnordost.streetcomplete.quests.road_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.POSTMAN
import de.westnordost.streetcomplete.quests.LocalizedName

class AddRoadName : OsmFilterQuestType<RoadNameAnswer>() {

    override val elementFilter = """
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
    """

    override val enabledInCountries = AllCountriesExcept("JP")
    override val changesetComment = "Determine road names and types"
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

    override fun createForm() = AddRoadNameForm()

    override fun applyAnswerTo(answer: RoadNameAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is NoRoadName -> tags["noname"] = "yes"
            is RoadIsServiceRoad -> {
                // The understanding of what is a service road is much broader in common language
                // than what the highway=service tagging covers. For example, certain traffic-calmed
                // driveways / service roads may be tagged as highway=living_street. We do not want
                // to overwrite this, so let's keep it a living street in that case (see #2431)
                if (tags["highway"] == "living_street") {
                    tags["noname"] = "yes"
                } else {
                    tags["highway"] = "service"
                }
            }
            is RoadIsTrack -> tags["highway"] = "track"
            is RoadIsLinkRoad -> {
                if (tags["highway"]?.matches("primary|secondary|tertiary".toRegex()) == true) {
                    tags["highway"] += "_link"
                }
            }
            is RoadName -> {
                val singleName = answer.localizedNames.singleOrNull()
                if (singleName?.isRef() == true) {
                    tags["ref"] = singleName.name
                } else {
                    applyAnswerRoadName(answer, tags)
                }
            }
        }
    }

    private fun applyAnswerRoadName(answer: RoadName, tags: Tags) {
        for ((languageTag, name) in answer.localizedNames) {
            val key = when (languageTag) {
                "" -> "name"
                "international" -> "int_name"
                else -> "name:$languageTag"
            }
            tags[key] = name
        }
    }
}

private fun LocalizedName.isRef() =
    languageTag.isEmpty() && name.matches("[A-Z]{0,3}[ -]?[0-9]{0,5}".toRegex())
