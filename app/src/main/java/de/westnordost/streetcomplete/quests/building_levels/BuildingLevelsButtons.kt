package de.westnordost.streetcomplete.quests.building_levels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** Row of buttons that shows (previous) building levels and roof levels */
@Composable
fun BuildingLevelsButtons(
    buildingLevels: List<BuildingLevels>,
    onSelect: (levels: Int, roofLevels: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(buildingLevels) { buildingLevel ->
            BuildingLevelsButton(
                onClick = { onSelect(buildingLevel.levels, buildingLevel.roofLevels) },
                levels = buildingLevel.levels,
                roofLevels = buildingLevel.roofLevels
            )
        }
    }
}

@Composable
@Preview
private fun BuildingLevelsButtonsPreview() {
    BuildingLevelsButtons(onSelect = { _, _ -> },
        buildingLevels = listOf(
            BuildingLevels(5, 2),
            BuildingLevels(4, 1),
            BuildingLevels(3, 0)
        ))
}
