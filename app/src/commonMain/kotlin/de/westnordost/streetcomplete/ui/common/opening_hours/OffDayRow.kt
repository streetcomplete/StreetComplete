package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_delete_24
import de.westnordost.streetcomplete.resources.quest_openingHours_delete
import de.westnordost.streetcomplete.resources.quest_openingHours_off_day
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Just a text "off day" with a delete button... */
@Composable
fun OffDayRow(
    timeTextWidth: Dp,
    onClickDelete: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = stringResource(Res.string.quest_openingHours_off_day),
            modifier = Modifier.width(timeTextWidth)
        )
        if (enabled) {
            IconButton(
                onClick = onClickDelete
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_delete_24),
                    contentDescription = stringResource(Res.string.quest_openingHours_delete)
                )
            }
        }
    }
}
