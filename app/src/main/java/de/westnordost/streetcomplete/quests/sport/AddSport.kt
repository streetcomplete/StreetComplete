package de.westnordost.streetcomplete.quests.sport

import android.os.Bundle
import android.text.TextUtils

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment

class AddSport(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = """
        nodes, ways with leisure=pitch and
        (!sport or sport ~ ${AMBIGUOUS_SPORT_VALUES.joinToString("|")} )
        and (access !~ private|no)
    """
    override val commitMessage = "Add pitches sport"
    override val icon = R.drawable.ic_quest_sport

    override fun getTitle(tags: Map<String, String>) = R.string.quest_sport_title

    override fun createForm() = AddSportForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val valuesStr = answer.getStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES)!!.joinToString(";")

        val prev = changes.getPreviousValue("sport")

        // only modify the previous values in case of these ~deprecated ones, otherwise assume
        // always that the tag has not been set yet (will drop the solution if it has been set
        // in the meantime by other people) (#291)
        if (AMBIGUOUS_SPORT_VALUES.contains(prev)) {
            changes.modify("sport", valuesStr)
        } else {
            changes.add("sport", valuesStr)
        }
    }

    companion object {
        private val AMBIGUOUS_SPORT_VALUES = arrayOf(
            "team_handball", // -> not really ambiguous but same as handball
            "hockey", // -> ice_hockey or field_hockey
            "skating", // -> ice_skating or roller_skating
            "football" // -> american_football, soccer or other *_football
        )
    }
}
