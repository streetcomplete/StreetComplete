package de.westnordost.streetcomplete.quests.building_levels

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.theme.AppTheme

@Composable
fun AddBuildingLevelsButton(lastLevels: Int, lastRoofLevels: Int?, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier) {
        Row(modifier = Modifier
            .height(52.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = lastLevels.toString(),
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Bottom)
            )
            Image(
                painterResource(R.drawable.ic_building_levels_illustration),
                "Building Illustration"
            )
            Text(
                text = lastRoofLevels?.toString() ?: " ",
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Top)
            )
        }
    }
}

@Composable
@Preview(showBackground = true,
    name = "Add Building Levels Button",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
fun PreviewAddBuildingLevelsButton() {
    AppTheme {
        AddBuildingLevelsButton(3, 1) {}
    }
}
