package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.cancel
import de.westnordost.streetcomplete.resources.ok
import de.westnordost.streetcomplete.resources.quest_openingHours_comment_description
import de.westnordost.streetcomplete.resources.quest_openingHours_comment_title
import org.jetbrains.compose.resources.stringResource
import kotlin.text.replace

/** Dialog to input an opening hours comment */
@Composable fun OpeningHoursCommentDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (comment: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var comment by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    if (comment.isNotEmpty()) {
                        onConfirm(comment.trim())
                        onDismissRequest()
                    }
                },
                enabled = comment.isNotEmpty()
            ) {
                Text(stringResource(Res.string.ok))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
        },
        title = { Text(stringResource(Res.string.quest_openingHours_comment_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    LocalTextStyle provides MaterialTheme.typography.body2
                ) {
                    Text(stringResource(Res.string.quest_openingHours_comment_description))
                }
                TextField(
                    value = comment,
                    onValueChange = {
                        val noDoubleQuotes = it.replace("\"", "")
                        if (noDoubleQuotes.length < 253) comment = noDoubleQuotes
                    }
                )
            }
        },
    )
}
