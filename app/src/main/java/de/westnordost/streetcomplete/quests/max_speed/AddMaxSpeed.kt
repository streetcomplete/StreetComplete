package de.westnordost.streetcomplete.quests.max_speed

import android.os.Bundle
import android.text.TextUtils

import javax.inject.Inject

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.Countries
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment

class AddMaxSpeed @Inject constructor(overpassServer: OverpassMapDataDao) :
    SimpleOverpassQuestType(overpassServer) {

    override// implicit speed limits
    // not any unpaved as they are unlikely developed enough to have speed limits signposted
    // neither private roads nor roads that are not for cars
    val tagFilters: String
        get() = "ways with highway ~ motorway|trunk|primary|secondary|tertiary|unclassified|residential" +
                " and !maxspeed and !maxspeed:forward and !maxspeed:backward" +
                " and !source:maxspeed and !zone:maxspeed and !maxspeed:type and !zone:traffic" +
                " and surface !~" + TextUtils.join("|", OsmTaggings.ANYTHING_UNPAVED) +
                " and motor_vehicle !~ private|no" +
                " and vehicle !~ private|no" +
                " and (access !~ private|no or (foot and foot !~ private|no))" +
                " and area != yes"

    override val commitMessage: String
        get() = "Add speed limits"
    override val icon: Int
        get() = R.drawable.ic_quest_max_speed

    override// see #813: US has different rules for each different state which need to be respected
    val enabledForCountries: Countries
        get() = Countries.allExcept(arrayOf("US"))

    override val defaultDisabledMessage: Int
        get() = R.string.default_disabled_msg_maxspeed

    override fun createForm(): AbstractQuestAnswerFragment {
        return AddMaxSpeedForm()
    }

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val isLivingStreet = answer.getBoolean(AddMaxSpeedForm.LIVING_STREET)
        val maxspeed = answer.getString(AddMaxSpeedForm.MAX_SPEED)
        val advisory = answer.getString(AddMaxSpeedForm.ADVISORY_SPEED)
        if (isLivingStreet) {
            changes.modify("highway", "living_street")
        } else if (advisory != null) {
            changes.add("maxspeed:advisory", advisory)
            changes.add("$MAXSPEED_TYPE:advisory", "sign")
        } else {
            if (maxspeed != null) {
                changes.add("maxspeed", maxspeed)
            }
            val country = answer.getString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_COUNTRY)
            val roadtype = answer.getString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_ROADTYPE)
            if (roadtype != null && country != null) {
                changes.add(MAXSPEED_TYPE, "$country:$roadtype")
            } else if (maxspeed != null) {
                changes.add(MAXSPEED_TYPE, "sign")
            }
        }
    }

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")

        return if (hasName)
            R.string.quest_maxspeed_name_title2
        else
            R.string.quest_maxspeed_title_short2
    }

    fun hasMarkersAtEnds(): Boolean {
        return true
    }

    companion object {
        private val MAXSPEED_TYPE = "maxspeed:type"
    }
}
