package de.westnordost.streetcomplete.quests.crossing

import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.crossing.CrossingAnswer.*
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddCrossingForm : AListQuestForm<CrossingAnswer, CrossingAnswer>() {
    private val mapDataSource: MapDataWithEditsSource by inject()

    override val items = CrossingAnswer.entries

    @Composable override fun BoxScope.ItemContent(item: CrossingAnswer) {
        Text(stringResource(item.text))
    }
    /*
        PROHIBITED is not possible for sidewalks or crossings (=separately mapped sidewalk
        infrastructure) because if the crossing does not exist, it would require to also
        delete/adapt the crossing ways, rather than just tagging crossing=no on the vertex.

        This situation needs to be solved in a different editor, so we ask the user to leave a note.
        See https://github.com/streetcomplete/StreetComplete/pull/2999#discussion_r681516203
        and https://github.com/streetcomplete/StreetComplete/issues/5160

        INFORMAL on the other hand would be okay because crossing=informal would not require
        deleting the crossing ways (I would say... it is in edge case...)
     */
    override fun onClickOk() {
        if (checkedItem.value == PROHIBITED && isOnSidewalkOrCrossing()) {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.quest_leave_new_note_as_answer)
                .setPositiveButton(R.string.quest_leave_new_note_yes) { _, _ -> composeNote() }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } else {
            super.onClickOk()
        }
    }

    private fun isOnSidewalkOrCrossing(): Boolean =
        mapDataSource.getWaysForNode(element.id).any {
            val footway = it.tags["footway"]
            footway == "sidewalk" || footway == "crossing"
        }
}
