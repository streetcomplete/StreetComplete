package de.westnordost.streetcomplete.quests.max_speed

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.Countries
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddMaxSpeed(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = """
        ways with highway ~ motorway|trunk|primary|secondary|tertiary|unclassified|residential
         and !maxspeed and !maxspeed:forward and !maxspeed:backward
         and !source:maxspeed and !zone:maxspeed and !maxspeed:type and !zone:traffic
         and surface !~ ${OsmTaggings.ANYTHING_UNPAVED.joinToString("|")}
         and motor_vehicle !~ private|no
         and vehicle !~ private|no
         and (access !~ private|no or (foot and foot !~ private|no))
         and area != yes
    """
    override val commitMessage = "Add speed limits"
    override val icon = R.drawable.ic_quest_max_speed
    override val hasMarkersAtEnds = true

    // see #813: US has different rules for each different state which need to be respected
    override val enabledForCountries: Countries = Countries.allExcept(arrayOf("US"))
    override val defaultDisabledMessage = R.string.default_disabled_msg_maxspeed

    override fun getTitle(tags: Map<String, String>) =
        if (tags.containsKey("name"))
            R.string.quest_maxspeed_name_title2
        else
            R.string.quest_maxspeed_title_short2

    override fun createForm() = AddMaxSpeedForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val isLivingStreet = answer.getBoolean(AddMaxSpeedForm.LIVING_STREET)
        val maxspeed = answer.getString(AddMaxSpeedForm.MAX_SPEED)
        val advisory = answer.getString(AddMaxSpeedForm.ADVISORY_SPEED)

        if (isLivingStreet) {
            changes.modify("highway", "living_street")
        } else if (advisory != null) {
            changes.add("maxspeed:advisory", advisory)
            changes.add("maxspeed:type:advisory", "sign")
        } else {
            if (maxspeed != null) {
                changes.add("maxspeed", maxspeed)
            }
            val country = answer.getString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_COUNTRY)
            val roadtype = answer.getString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_ROADTYPE)
            if (roadtype != null && country != null) {
                changes.add("maxspeed:type", "$country:$roadtype")
            } else if (maxspeed != null) {
                changes.add("maxspeed:type", "sign")
            }
        }
    }
}
