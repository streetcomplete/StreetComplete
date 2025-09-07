package de.westnordost.streetcomplete.quests.building_levels

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_building_levels_illustration
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/** A button that shows (previous) building levels and roof levels */
@Composable
fun BuildingLevelsButton(
    onClick: () -> Unit,
    levels: Int,
    roofLevels: Int?,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = levels.toString(),
                style = MaterialTheme.typography.body2,
                modifier = Modifier.align(Alignment.Bottom)
            )
            Image(
                painter = painterResource(Res.drawable.ic_building_levels_illustration),
                contentDescription = "Building Illustration"
            )
            Text(
                text = roofLevels?.toString() ?: " ",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.align(Alignment.Top)
            )
        }
    }
}

@Composable
@Preview
private fun BuildingLevelsButtonPreview() {
    BuildingLevelsButton({}, 3, 1)
}
