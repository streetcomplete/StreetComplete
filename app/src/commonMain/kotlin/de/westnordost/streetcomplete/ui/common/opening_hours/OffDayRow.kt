package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_openingHours_off_day
import org.jetbrains.compose.resources.stringResource

/** Just a text "off day" with a delete button... */
@Composable
fun OffDayRow(
    onClickDelete: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 48.dp)
                .padding(8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = stringResource(Res.string.quest_openingHours_off_day),
            )
        }
        DeleteRowButton(
            onClick = onClickDelete,
            visible = enabled,
        )
    }
}
