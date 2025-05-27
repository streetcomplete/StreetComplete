package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeColorCircle
import de.westnordost.streetcomplete.ui.common.MenuIcon

@Composable
fun MainMenuButton(
    onClick: () -> Unit,
    unsyncedEditsCount: Int,
    modifier: Modifier = Modifier,
    indexInTeam: Int? = null,
) {
    Box(modifier) {
        MapButton(onClick = onClick) { MenuIcon() }
        if (indexInTeam != null) {
            TeamModeColorCircle(
                index = indexInTeam,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
            )
        }
        if (unsyncedEditsCount > 0) {
            Box(Modifier.align(Alignment.TopEnd)) {
                NotificationBox {
                    Text(unsyncedEditsCount.toString(), textAlign = TextAlign.Center)
                }
            }
        }
    }
}
