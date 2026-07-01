package de.westnordost.streetcomplete.screens.main.edithistory

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.edithistory.Edit
import org.jetbrains.compose.resources.painterResource

/** Icon representing an edit (main icon + overlay icon) */
@Composable
fun EditImage(
    edit: Edit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier) {
        edit.icon?.let {
            Image(painterResource(it), null)
        }
        edit.overlayIcon?.let {
            Image(
                painter = painterResource(it),
                contentDescription = null,
                modifier = Modifier
                    .size(maxWidth * 0.75f)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}
