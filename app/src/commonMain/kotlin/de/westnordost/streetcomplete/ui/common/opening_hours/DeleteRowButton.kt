package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_delete_24
import de.westnordost.streetcomplete.resources.quest_openingHours_delete
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** An icon button for deleting a(n opening hours) row, can be invisible (still takes the same space) */
@Composable
fun DeleteRowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    if (visible) {
        IconButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_delete_24),
                contentDescription = stringResource(Res.string.quest_openingHours_delete)
            )
        }
    } else {
        Spacer(modifier.size(48.dp))
    }
}
