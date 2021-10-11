package de.westnordost.streetcomplete.quests.max_speed

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.data.meta.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR

class AddMaxSpeed : OsmFilterQuestType<MaxSpeedAnswer>() {

    override val elementFilter = """
        ways with highway ~ motorway|trunk|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential
         and !maxspeed and !maxspeed:advisory and !maxspeed:forward and !maxspeed:backward
         and ${MAXSPEED_TYPE_KEYS.joinToString(" and ") { "!$it" }}
         and surface !~ ${ANYTHING_UNPAVED.joinToString("|")}
         and cyclestreet != yes and bicycle_road != yes
         and motor_vehicle !~ private|no
         and vehicle !~ private|no
         and area != yes
         and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val commitMessage = "Add speed limits"
    override val wikiLink = "Key:maxspeed"
    override val icon = R.drawable.ic_quest_max_speed
    override val hasMarkersAtEnds = true
    override val isSplitWayEnabled = true

    // see #813: US has different rules for each different state which need to be respected
    override val enabledInCountries = AllCountriesExcept("US")
    override val defaultDisabledMessage = R.string.default_disabled_msg_maxspeed

    override val questTypeAchievements = listOf(CAR)

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
                // Lit is either already set or has been answered by the user, so this wouldn't change the value of the lit tag
                if (answer.lit != null) {
                    if (answer.lit) {
                        changes.addOrModify("lit", "yes")
                    } else {
                        changes.addOrModify("lit", "no")
                    }
                }
            }
        }
    }
}
