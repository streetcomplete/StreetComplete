package de.westnordost.streetcomplete.screens.main.edithistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType
import de.westnordost.streetcomplete.ui.common.UndoIcon

/** One item in the edit history sidebar list. Selectable and when selected, an undo button is
 *  clickable. */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EditHistoryItem(
    selected: Boolean,
    onSelect: () -> Unit,
    onUndo: () -> Unit,
    edit: Edit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when {
        selected -> MaterialTheme.colors.secondary.copy(alpha = 0.5f)
        edit.isSynced == true -> MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
        else -> MaterialTheme.colors.surface
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(backgroundColor)
            .selectable(
                selected = selected,
                onClick = onSelect
            ),
    ) {
        Box(Modifier
            .size(56.dp)
            .padding(4.dp)
        ) {
            EditImage(edit)
            if (selected) {
                val undoButtonColor = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.surface
                )
                Surface(
                    onClick = onUndo,
                    modifier = Modifier.align(Alignment.Center),
                    shape = CircleShape,
                    color = undoButtonColor.backgroundColor(edit.isUndoable).value,
                    contentColor = undoButtonColor.contentColor(edit.isUndoable).value,
                    elevation = 4.dp
                ) {
                    Box(Modifier.padding(8.dp)) { UndoIcon() }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewEditsColumnItem() {
    var selected by remember { mutableStateOf(false) }
    EditHistoryItem(
        selected = selected,
        onSelect = { selected = !selected },
        onUndo = {},
        modifier = Modifier.width(80.dp),
        edit = OsmQuestHidden(ElementType.NODE, 1L, AddRecyclingType(), LatLon(0.0,0.0), 1L),
    )
}
