package de.westnordost.streetcomplete.quests.max_speed

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.data.osm.AllCountriesExcept

class AddMaxSpeed(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<MaxSpeedAnswer>(o) {

    override val tagFilters = """
        ways with highway ~ motorway|trunk|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential
         and !maxspeed and !maxspeed:forward and !maxspeed:backward
         and !source:maxspeed and !zone:maxspeed and !maxspeed:type and !zone:traffic
         and surface !~ ${OsmTaggings.ANYTHING_UNPAVED.joinToString("|")}
         and motor_vehicle !~ private|no
         and vehicle !~ private|no
         and area != yes
         and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val commitMessage = "Add speed limits"
    override val icon = R.drawable.ic_quest_max_speed
    override val hasMarkersAtEnds = true
    override val isSplitWayEnabled = true

    // see #813: US has different rules for each different state which need to be respected
    override val enabledInCountries = AllCountriesExcept("US")
    override val defaultDisabledMessage = R.string.default_disabled_msg_maxspeed

    override fun getTitle(tags: Map<String, String>) =
        if (tags.containsKey("name"))
            R.string.quest_maxspeed_name_title2
        else
            R.string.quest_maxspeed_title_short2

    override fun createForm() = AddMaxSpeedForm()

    override fun applyAnswerTo(answer: MaxSpeedAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is MaxSpeedSign -> {
                changes.add("maxspeed", answer.value.toString())
                changes.add("maxspeed:type", "sign")
            }
            is MaxSpeedZone -> {
                changes.add("maxspeed", answer.value.toString())
                changes.add("maxspeed:type", answer.countryCode + ":" + answer.roadType)
            }
            is AdvisorySpeedSign -> {
                changes.add("maxspeed:advisory", answer.value.toString())
                changes.add("maxspeed:type:advisory", "sign")
            }
            is IsLivingStreet -> {
                changes.modify("highway", "living_street")
            }
            is ImplicitMaxSpeed -> {
                changes.add("maxspeed:type", answer.countryCode + ":" + answer.roadType)
            }
        }
    }
}
