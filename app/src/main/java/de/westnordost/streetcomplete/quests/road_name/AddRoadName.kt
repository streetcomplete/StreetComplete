package de.westnordost.streetcomplete.quests.road_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.POSTMAN
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.applyTo

class AddRoadName : OsmFilterQuestType<RoadNameAnswer>() {

    override val elementFilter = """
        ways with
          highway ~ primary|secondary|tertiary|unclassified|residential|living_street|pedestrian|busway
          and !name and !name:left and !name:right
          and !ref
          and noname != yes
          and name:signed != no
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
    override val achievements = listOf(CAR, PEDESTRIAN, POSTMAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_streetName_title

    override fun createForm() = AddRoadNameForm()

    override fun applyAnswerTo(answer: RoadNameAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
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
                    answer.localizedNames.applyTo(tags)
                }
            }
        }
    }
}

private fun LocalizedName.isRef() =
    languageTag.isEmpty() && name.matches("[A-Z]{0,3}[ -]?[0-9]{0,5}".toRegex())
