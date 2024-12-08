package de.westnordost.streetcomplete.quests.building_levels

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AddBuildingLevelsSavedButtons(
    buildingLevels: List<BuildingLevelsAnswer>,
    onButton: (Int, Int?) -> Unit,
) {
    LazyRow(modifier = Modifier.defaultMinSize(minHeight = 52.dp)) {
        items(buildingLevels.size) { position ->
            val curLevel = buildingLevels[position]
            AddBuildingLevelsButton(
                curLevel.levels,
                curLevel.roofLevels,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                onButton(curLevel.levels, curLevel.roofLevels)
            }
        }
    }
}

@Composable
@Preview
fun AddBuildingLevelsSavedButtonsPreview() {
    AddBuildingLevelsSavedButtons(onButton = { reg, roof -> },
        buildingLevels = listOf(
            BuildingLevelsAnswer(5, 2),
            BuildingLevelsAnswer(4, 1),
            BuildingLevelsAnswer(3, 0)
        ))
}
