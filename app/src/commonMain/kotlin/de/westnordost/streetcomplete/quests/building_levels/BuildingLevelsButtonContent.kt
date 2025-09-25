package de.westnordost.streetcomplete.quests.building_levels

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.building_levels_illustration_icon
import org.jetbrains.compose.resources.painterResource

/** Content for a button that shows (previous) building levels and roof levels */
@Composable
fun BuildingLevelsButtonContent(
    levels: Int,
    roofLevels: Int?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = levels.toString(),
            style = MaterialTheme.typography.body2,
            modifier = Modifier.align(Alignment.Bottom)
        )
        Image(
            painter = painterResource(Res.drawable.building_levels_illustration_icon),
            contentDescription = "Building Illustration"
        )
        Text(
            text = roofLevels?.toString() ?: " ",
            style = MaterialTheme.typography.body2,
            modifier = Modifier.align(Alignment.Top)
        )
    }
}
