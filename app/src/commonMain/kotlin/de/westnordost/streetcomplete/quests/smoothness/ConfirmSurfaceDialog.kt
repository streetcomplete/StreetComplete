package de.westnordost.streetcomplete.quests.smoothness

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Dialog that asks the user whether the presented surface is correct. */
@Composable
fun ConfirmSurfaceDialog(
    onDismissRequest: () -> Unit,
    surface: Surface,
    onConfirmSurface: () -> Unit,
    onWrongSurface: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        buttons = {
            TextButton(onClick = { onConfirmSurface(); onDismissRequest() }) {
                Text(stringResource(Res.string.quest_generic_hasFeature_yes_leave_note))
            }
            TextButton(onClick = { onWrongSurface(); onDismissRequest() }) {
                Text(stringResource(Res.string.quest_generic_hasFeature_no))
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stringResource(Res.string.quest_smoothness_surface_value))
                ImageWithLabel(
                    painter = surface.icon?.let { painterResource(it) },
                    label = stringResource(surface.title),
                )
                Text(stringResource(Res.string.quest_address_answer_no_housenumber_message2b))
            }
        }
    )
}
