package de.westnordost.streetcomplete.quests.building_levels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.theme.largeInput

/** Form to input building levels and roof levels, with quick-select buttons */
@Composable
fun BuildingLevelsForm(
    levels: String,
    onLevelsChange: (String) -> Unit,
    roofLevels: String,
    onRoofLevelsChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    previousBuildingLevels: List<BuildingLevels> = listOf(),
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.height(IntrinsicSize.Min)) {
            BuildingLevelsIllustration(Modifier.fillMaxSize())
            Row {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.quest_buildingLevels_levelsLabel2),
                        style = MaterialTheme.typography.caption.copy(hyphens = Hyphens.Auto),
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    )
                    TextField2(
                        value = levels,
                        onValueChange = onLevelsChange,
                        isError = levels.isNotEmpty() && !levels.isValidLevel(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.largeInput.copy(
                            textAlign = TextAlign.Center
                        ),
                    )
                }
                // same height as BuildingLevelsIllustration
                Spacer(Modifier.size(188.dp, 144.dp))
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    TextField2(
                        value = roofLevels,
                        onValueChange = onRoofLevelsChange,
                        isError = roofLevels.isNotEmpty() && !roofLevels.isValidLevel(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.largeInput.copy(
                            textAlign = TextAlign.Center
                        ),
                    )
                    Text(
                        text = stringResource(R.string.quest_buildingLevels_roofLevelsLabel2),
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.caption.copy(hyphens = Hyphens.Auto),
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    )
                }
            }
        }
        BuildingLevelsButtons(
            buildingLevels = previousBuildingLevels,
            onSelect = { levels, roofLevels ->
                onLevelsChange(levels.toString())
                onRoofLevelsChange(roofLevels?.toString() ?: "")
            }
        )
    }
}

private fun String.isValidLevel(): Boolean =
    toIntOrNull()?.takeIf { it >= 0 } != null

@Composable
@Preview(showBackground = true, locale = "de", widthDp = 320)
@Preview(showBackground = true, locale = "ar")
private fun BuildingLevelsFormPreview() {
    val levels = remember { mutableStateOf("55") }
    val roofLevels = remember { mutableStateOf("55") }
    BuildingLevelsForm(
        levels = levels.value,
        onLevelsChange = { levels.value = it },
        roofLevels = roofLevels.value,
        onRoofLevelsChange = { roofLevels.value = it },
        previousBuildingLevels = listOf(
            BuildingLevels(5, 2),
            BuildingLevels(4, 1),
            BuildingLevels(3, 0)
        ),
    )
}
