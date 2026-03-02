package de.westnordost.streetcomplete.quests.max_speed

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.maxspeed.MAX_SPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.surface.UNPAVED_SURFACES
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_maxspeed
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddMaxSpeed (
    private val getCountryOrSubdivisionCode: (LatLon) -> String?
) : OsmFilterQuestType<MaxSpeedAnswer>(), AndroidQuest {

    override val elementFilter = """
        ways with
         highway ~ motorway|trunk|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|busway
         and !maxspeed and !maxspeed:advisory and !maxspeed:forward and !maxspeed:backward
         and ${MAX_SPEED_TYPE_KEYS.joinToString(" and ") { "!$it" }}
         and surface !~ ${UNPAVED_SURFACES.joinToString("|")}
         and cyclestreet != yes and bicycle_road != yes
         and living_street != yes
         and motor_vehicle !~ private|no
         and vehicle !~ private|no
         and area != yes
         and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val changesetComment = "Specify speed limits"
    override val wikiLink = "Key:maxspeed"
    override val icon = R.drawable.quest_max_speed
    override val hasMarkersAtEnds = true
    override val achievements = listOf(CAR)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_maxspeed

    override fun getTitle(tags: Map<String, String>) = R.string.quest_maxspeed_title_short2

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with traffic_sign = city_limit")

    override fun createForm() = AddMaxSpeedForm()

    override fun applyAnswerTo(answer: MaxSpeedAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags, getCountryOrSubdivisionCode(geometry.center) ?: "??")
    }
}
