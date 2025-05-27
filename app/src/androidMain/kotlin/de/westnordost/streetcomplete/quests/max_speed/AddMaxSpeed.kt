package de.westnordost.streetcomplete.quests.max_speed

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.UNPAVED_SURFACES
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddMaxSpeed : OsmFilterQuestType<MaxSpeedAnswer>() {

    override val elementFilter = """
        ways with
         highway ~ motorway|trunk|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|busway
         and !maxspeed and !maxspeed:advisory and !maxspeed:forward and !maxspeed:backward
         and ${MAXSPEED_TYPE_KEYS.joinToString(" and ") { "!$it" }}
         and surface !~ ${UNPAVED_SURFACES.joinToString("|")}
         and cyclestreet != yes and bicycle_road != yes
         and motor_vehicle !~ private|no
         and vehicle !~ private|no
         and area != yes
         and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val changesetComment = "Specify speed limits"
    override val wikiLink = "Key:maxspeed"
    override val icon = R.drawable.ic_quest_max_speed
    override val hasMarkersAtEnds = true
    // see #813: US has different rules for each different state which need to be respected
    override val enabledInCountries = AllCountriesExcept("US")
    override val achievements = listOf(CAR)
    override val defaultDisabledMessage = R.string.default_disabled_msg_maxspeed

    override fun getTitle(tags: Map<String, String>) = R.string.quest_maxspeed_title_short2

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with traffic_sign = city_limit")

    override fun createForm() = AddMaxSpeedForm()

    override fun applyAnswerTo(answer: MaxSpeedAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is MaxSpeedSign -> {
                tags["maxspeed"] = answer.value.toString()
                tags["maxspeed:type"] = "sign"
            }
            is MaxSpeedZone -> {
                tags["maxspeed"] = answer.value.toString()
                tags["maxspeed:type"] = answer.countryCode + ":" + answer.roadType
            }
            is AdvisorySpeedSign -> {
                tags["maxspeed:advisory"] = answer.value.toString()
                tags["maxspeed:type:advisory"] = "sign"
            }
            is IsLivingStreet -> {
                tags["highway"] = "living_street"
            }
            is ImplicitMaxSpeed -> {
                tags["maxspeed:type"] = answer.countryCode + ":" + answer.roadType
                // Lit is either already set or has been answered by the user, so this wouldn't change the value of the lit tag
                answer.lit?.let { tags["lit"] = it.toYesNo() }
            }
        }
    }
}
