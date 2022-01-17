package de.westnordost.streetcomplete.quests.shoulder

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.data.meta.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR


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
            highway = trunk
            or (
              highway ~ primary|secondary|tertiary|unclassified
              and (
                motorroad = yes
                or tunnel ~ yes|building_passage|avalanche_protector
                or bridge = yes
                or sidewalk ~ no|none
                or maxspeed > 50
                or maxspeed ~ "([4-9][0-9]|1[0-9][0-9]) mph"
                or ~${(MAXSPEED_TYPE_KEYS + "maxspeed").joinToString("|")} ~ ".*(rural|trunk|motorway|nsl_single|nsl_dual)"
              )
            )
          )
          and lane_markings != no
          and surface !~ ${ANYTHING_UNPAVED.joinToString("|")}
          and (!parking:lane or parking:lane ~ no|none|no_stopping|no_parking|separate)
          and (!parking:lane:left or parking:lane:left ~ no|none|no_stopping|no_parking|separate)
          and (!parking:lane:right or parking:lane:right ~ no|none|no_stopping|no_parking|separate)
          and (!parking:lane:both or parking:lane:both ~ no|none|no_stopping|no_parking|separate)
          and cycleway !~ lane|opposite_lane
          and cycleway:left !~ lane|opposite_lane
          and cycleway:right !~ lane|opposite_lane
          and cycleway:both !~ lane|opposite_lane
          and !shoulder
          and !shoulder:left and !shoulder:right and !shoulder:both
          and (access !~ private|no or (foot and foot !~ private|no))
    """

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter(elementFilter).asIterable()

    override val changesetComment = "Add whether there are shoulders"
    override val wikiLink = "Key:shoulder"
    override val icon = R.drawable.ic_quest_street_shoulder
    override val isSplitWayEnabled = true
    override val questTypeAchievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_shoulder_title

    override fun createForm() = AddShoulderForm()

    override fun applyAnswerTo(answer: ShoulderSides, tags: Tags, timestampEdited: Long) {
        tags["shoulder"] = answer.osmValue
    }
}
