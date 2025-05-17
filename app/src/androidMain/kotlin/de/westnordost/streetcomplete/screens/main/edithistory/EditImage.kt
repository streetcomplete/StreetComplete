package de.westnordost.streetcomplete.screens.main.edithistory

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import de.westnordost.streetcomplete.data.edithistory.Edit

/** Icon representing an edit (main icon + overlay icon) */
@Composable
fun EditImage(
    edit: Edit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier) {
        val editIcon = edit.icon
        if (editIcon != 0) {
            Image(painterResource(edit.icon), null)
        }
        val overlayIcon = edit.overlayIcon
        if (overlayIcon != 0) {
            Image(
                painter = painterResource(overlayIcon),
                contentDescription = null,
                modifier = Modifier
                    .size(maxWidth * 0.75f)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}
