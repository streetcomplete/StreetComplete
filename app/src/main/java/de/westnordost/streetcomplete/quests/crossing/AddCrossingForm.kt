package de.westnordost.streetcomplete.quests.crossing

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.crossing.CrossingAnswer.*
import org.koin.android.ext.android.inject

class AddCrossingForm : AListQuestForm<CrossingAnswer>() {
    private val mapDataSource: MapDataWithEditsSource by inject()

    override val items get() = listOf(
        TextItem(YES, R.string.quest_crossing_yes),
        TextItem(NO, R.string.quest_crossing_no),
        TextItem(PROHIBITED, R.string.quest_crossing_prohibited)
    )

    /* PROHIBITED is neither possible for sidewalks nor crossings (=separately mapped sidewalk infrastructure)
    *  because a "no" answer would require to also delete/adapt the crossing ways, rather than just
    *  tagging crossing=no on the vertex.
    *  This situation needs to be solved in a different editor, so we ask the user to leave a note.
    *  See https://github.com/streetcomplete/StreetComplete/pull/2999#discussion_r681516203
    *  and https://github.com/streetcomplete/StreetComplete/issues/5160 */
    override fun isFormComplete(): Boolean {
        if (checkedItem?.value == PROHIBITED && isOnSidewalkOrCrossing()) {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.quest_crossing_prohibited_but_on_sidewalk_or_crossing)
                .setPositiveButton(R.string.quest_leave_new_note_yes) { _, _ -> composeNote() }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            return false
        }
        return super.isFormComplete()
    }

    override fun isRejectingClose() = super.isFormComplete()

    private fun isOnSidewalkOrCrossing(): Boolean {
        val ways = mapDataSource.getWaysForNode(element.id)
        return ways.any {
            val footway = it.tags["footway"]
            footway == "sidewalk" || footway == "crossing"
        }
    }
}
