package de.westnordost.streetcomplete.quests.shoulder

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.UNPAVED_SURFACES

class AddShoulder : OsmFilterQuestType<ShoulderSides>() {

    /* Trunks always, smaller roads only if they are either motorroads, bridges or tunnels or if
     * they are likely rural roads (high max speeds - implicit or explicit - or no sidewalk).
     *
     * The existence of a parking lane, cycle lane excludes the existence of a shoulder because they
     * are in themselves kind of a shoulder (with a special purpose)
     *
     * */
    override val elementFilter = """
        ways with
          (
            (
              highway ~ trunk|primary|secondary|tertiary|unclassified
              and (
                motorroad = yes
                or expressway = yes
                or tunnel ~ yes|building_passage|avalanche_protector
                or bridge = yes
                or sidewalk ~ no|none
                or !maxspeed and highway = trunk
                or maxspeed > 50
                or ~"${(MAXSPEED_TYPE_KEYS + "maxspeed").joinToString("|")}" ~ ".*:(rural|trunk|motorway|nsl_single|nsl_dual)"
              )
            ) or (
              highway ~ motorway|motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified
              and (foot ~ yes|designated or bicycle ~ yes|designated)
            )
          )
          and lane_markings != no
          and surface !~ ${UNPAVED_SURFACES.joinToString("|")}
          and (!parking:lane or parking:lane ~ no|none|no_stopping|no_parking|separate)
          and (!parking:lane:left or parking:lane:left ~ no|none|no_stopping|no_parking|separate)
          and (!parking:lane:right or parking:lane:right ~ no|none|no_stopping|no_parking|separate)
          and (!parking:lane:both or parking:lane:both ~ no|none|no_stopping|no_parking|separate)
          and cycleway != lane
          and cycleway:left != lane
          and cycleway:right != lane
          and cycleway:both != lane
          and !verge
          and !shoulder
          and !shoulder:left and !shoulder:right and !shoulder:both
          and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val changesetComment = "Specify whether roads have shoulders"
    override val wikiLink = "Key:shoulder"
    override val icon = R.drawable.ic_quest_street_shoulder
    override val achievements = listOf(CAR)

    override val hint = R.string.quest_shoulder_explanation2

    override fun getTitle(tags: Map<String, String>) = R.string.quest_shoulder_title

    override fun createForm() = AddShoulderForm()

    override fun applyAnswerTo(answer: ShoulderSides, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["shoulder"] = answer.osmValue
    }
}
