package de.westnordost.streetcomplete.screens.main.edithistory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog

/** Confirmation dialog for undoing an edit. Shows details about an edit */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UndoDialog(
    edit: Edit,
    element: Element?,
    featureDictionaryLazy: Lazy<FeatureDictionary>,
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
) {
    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.undo_confirm_title2)) },
        content = {
            Box(Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                EditDetails(edit, element, featureDictionaryLazy)
            }
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.undo_confirm_negative))
            }
            TextButton(onClick = { onConfirmed(); onDismissRequest() }) {
                Text(stringResource(R.string.undo_confirm_positive))
            }
        },
        height = 360.dp
    )
}
