package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeColorCircle
import de.westnordost.streetcomplete.ui.common.MenuIcon

@Composable
fun MainMenuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    indexInTeam: Int? = null,
) {
    Box(modifier) {
        MapButton(onClick = onClick) { MenuIcon() }
        if (indexInTeam != null) {
            TeamModeColorCircle(
                index = indexInTeam,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
            )
        }
    }
}
