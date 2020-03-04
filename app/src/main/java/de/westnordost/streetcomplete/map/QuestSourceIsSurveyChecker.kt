package de.westnordost.streetcomplete.map

import android.content.Context
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.QuestGroup
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao
import de.westnordost.streetcomplete.util.crossTrackDistanceTo
import javax.inject.Inject

/** Checks if the quest was solved on a survey, either by looking at the GPS position or asking
 *  the user  */
class QuestSourceIsSurveyChecker @Inject constructor(
    private val osmQuestDB: OsmQuestDao,
    private val osmNoteQuestDao: OsmNoteQuestDao
) {
    fun assureIsSurvey(context: Context, questId: Long, group: QuestGroup, locations: List<Location>, isSurveyCallback: () -> Unit) {
        if (dontShowAgain || isWithinSurveyDistance(questId, group, locations)) {
	        isSurveyCallback()
        } else {
            val inner = LayoutInflater.from(context).inflate(R.layout.quest_source_dialog_layout, null, false)
            val checkBox = inner.findViewById<CheckBox>(R.id.checkBoxDontShowAgain)
	        checkBox.visibility = if (timesShown < 1) View.GONE else View.VISIBLE

	        AlertDialog.Builder(context)
                .setTitle(R.string.quest_source_dialog_title)
                .setView(inner)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                    ++timesShown
                    dontShowAgain = checkBox.isChecked
	                isSurveyCallback()
                }
                .setNegativeButton(android.R.string.cancel, null)
		        .show()
        }
    }

    private fun isWithinSurveyDistance(questId: Long, group: QuestGroup, locations: List<Location>): Boolean {
        val geometry = getQuestGeometry(questId, group) ?: return false
        for (location in locations) {
            val pos = OsmLatLon(location.latitude, location.longitude)
	        val polyLines: List<List<LatLon>> = when (geometry) {
	            is ElementPolylinesGeometry -> geometry.polylines
		        is ElementPolygonsGeometry -> geometry.polygons
		        else -> listOf(listOf(geometry.center))
	        }
            for (polyLine in polyLines) {
                val distance = pos.crossTrackDistanceTo(polyLine)
                if (distance < location.accuracy + MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY) return true
            }
        }
        return false
    }

    private fun getQuestGeometry(questId: Long, group: QuestGroup): ElementGeometry? =
        when (group) {
            QuestGroup.OSM -> osmQuestDB.get(questId)?.geometry
            QuestGroup.OSM_NOTE -> osmNoteQuestDao.get(questId)?.geometry
        }

    companion object {
        /*
        Considerations for choosing these values:

		- users should be encouraged to *really* go right there and check even if they think they
		  see it from afar already

		- just having walked by something should though still count as survey though. (It might be
		  inappropriate or awkward to stop and flip out the smartphone directly there)

		- GPS position might not be updated right after they fetched it out of their pocket, but GPS
		  position should be reset to "unknown" (instead of "wrong") when switching back to the app

		- the distance is the minimum distance between the quest geometry (i.e. a road) and the line
		  between the user's position when he opened the quest form and the position when he pressed
		  "ok", MINUS the current GPS accuracy, so it is a pretty forgiving calculation already
		*/

        private const val MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY = 80f //m

	    // "static" values persisted per application start
        private var dontShowAgain = false
        private var timesShown = 0
    }
}
