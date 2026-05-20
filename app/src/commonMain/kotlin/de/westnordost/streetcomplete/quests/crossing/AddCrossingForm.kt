package de.westnordost.streetcomplete.quests.crossing

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.quests.crossing.CrossingAnswer.PROHIBITED
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AddCrossingForm(
    onAnswer: (CrossingAnswer) -> Unit,
    element: Element,
    mapDataSource: MapDataWithEditsSource = koinInject()
) {
    var confirmLeaveNote by remember { mutableStateOf(false) }

    fun isNodeOnSidewalkOrCrossing(node: Node): Boolean =
        mapDataSource.getWaysForNode(node.id).any {
            val footway = it.tags["footway"]
            footway == "sidewalk" || footway == "crossing"
        }

    RadioGroupQuestForm(
        items = CrossingAnswer.entries,
        itemContent = { Text(stringResource(it.text)) },
        onClickOk = { selectedItem ->
            /*
            PROHIBITED is not possible for sidewalks or crossings (=separately mapped sidewalk
            infrastructure) because if the crossing does not exist, it would require to also
            delete/adapt the crossing ways, rather than just tagging crossing=no on the vertex.

            This situation needs to be solved in a different editor, so we ask the user to leave
            a note.
            See https://github.com/streetcomplete/StreetComplete/pull/2999#discussion_r681516203
            and https://github.com/streetcomplete/StreetComplete/issues/5160

            INFORMAL on the other hand would be okay because crossing=informal would not require
            deleting the crossing ways (I would say... it is in edge case...)
            */
            if (selectedItem == PROHIBITED && isNodeOnSidewalkOrCrossing(element as Node)) {
                confirmLeaveNote = true
            } else {
                onAnswer(selectedItem)
            }
        }
    )

    if (confirmLeaveNote) {
        ConfirmationDialog(
            onDismissRequest = { confirmLeaveNote = false },
            onConfirmed = { composeNote() },
            confirmButtonText = stringResource(Res.string.quest_leave_new_note_yes),
            text = { Text(stringResource(Res.string.quest_leave_new_note_as_answer)) }
        )
    }
}
