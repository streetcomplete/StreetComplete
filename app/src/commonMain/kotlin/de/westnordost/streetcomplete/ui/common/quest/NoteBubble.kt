package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.theme.defaultTextLinkStyles
import de.westnordost.streetcomplete.ui.theme.titleSmall
import de.westnordost.streetcomplete.ui.util.annotateLinks
import org.jetbrains.compose.resources.stringResource

/** Speech bubble (without arrow) that contains a note another user left for this object */
@Composable
fun NoteBubble(
    text: String,
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = elevation,
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = stringResource(Res.string.note_for_object),
                style = MaterialTheme.typography.titleSmall
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                SelectionContainer {
                    Text(
                        text = text.annotateLinks(MaterialTheme.typography.defaultTextLinkStyles()),
                        style = MaterialTheme.typography.body2,
                    )
                }
            }
        }
    }
}
