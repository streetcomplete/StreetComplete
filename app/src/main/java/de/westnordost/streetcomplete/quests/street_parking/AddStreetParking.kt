package de.westnordost.streetcomplete.quests.parking_lanes

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR


class AddStreetParking : OsmFilterQuestType<Unit>() {

    // TODO filter out near parking lanes mapped as areas?! And/or display them ?

    override val elementFilter = """
        ways with
          (
            highway = residential
            or (
              highway ~ primary|secondary|tertiary|unclassified
              and (
                sidewalk ~ both|left|right|yes|separate
                or ~${(MAXSPEED_TYPE_KEYS + "maxspeed").joinToString("|")} ~ .*urban|.*zone.*
                or maxspeed <= 60
                or maxspeed ~ "(5|10|15|20|25|30|35) mph"
              )
            )
          )
          and !parking:lane and !parking:lane:left and !parking:lane:right and !parking:lane:both
          and area != yes
          and motorroad != yes
          and priority_road !~ designated|yes
          and junction != roundabout
          and !turn:lanes and !turn:lanes:forward and !turn:lanes:backward and !turn:lanes:both_ways
          and (
            access !~ private|no
            or foot and foot !~ private|no
          )
    """

    /* On some roads, usually no-parking rules apply implicitly, so these are filtered out:
       - motorways, trunks (motorroads)
       - often priority roads (at least rural ones, but let's filter out all for now)
       - roundabouts
       - on sections of the roadway marked with arrows (turn lanes)

       There are some more rules which cannot be filtered due to the lack of tags for that that are
       set on the road-way:
       - in front of important signs (STOP, saltires, yield etc)
       - at taxi stands, bus stops, ...
       - on and near crossings, level crossings, ... on tram tracks (duh!) etc
       - at narrow points, sharp bends, fire rescue paths and other dangerous points
       - etc

       Further, to ask outside of urban areas does not really make sense, so we fuzzily exclude
       roads that are probably outside of settlements (similar idea like for AddWayLit)
      */

    override val commitMessage = "Add how cars park here"
    override val wikiLink = "Key:parking:lane"
    override val icon = R.drawable.ic_quest_parking_lane
    override val isSplitWayEnabled = true

    override val questTypeAchievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_street_parking_title

    override fun createForm() = AddStreetParkingForm()

    override fun applyAnswerTo(answer: Unit, changes: StringMapChangesBuilder) {
        TODO("Not yet implemented")
    }
}

