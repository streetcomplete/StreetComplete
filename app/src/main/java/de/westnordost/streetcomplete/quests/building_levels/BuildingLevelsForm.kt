package de.westnordost.streetcomplete.quests.building_levels

import androidx.compose.foundation.border
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
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.ktx.conditional
import de.westnordost.streetcomplete.ui.theme.titleLarge

/** Form to input building levels and roof levels, with quick-select buttons */
@Composable
fun BuildingLevelsForm(
    levels: String?,
    onLevels: (String) -> Unit,
    roofLevels: String?,
    onRoofLevels: (String) -> Unit,
    onButton: (levels: Int, roofLevels: Int?) -> Unit,
    modifier: Modifier = Modifier,
    previousBuildingLevels: List<BuildingLevels> = listOf(),
) {
    val focusRequester = remember { FocusRequester() }

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
                        style = MaterialTheme.typography.body2,
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    )
                    OutlinedTextField(
                        value = levels ?: "",
                        onValueChange = onLevels,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .conditional(levels == null) { focusRequester(focusRequester) }
                            .conditional(levels == null || !levels.isDigitsOnly()) { border(2.dp, color = MaterialTheme.colors.error) },
                        textStyle = MaterialTheme.typography.titleLarge.copy(
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
                    OutlinedTextField(
                        roofLevels ?: "",
                        onValueChange = onRoofLevels,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .conditional(levels != null) { focusRequester(focusRequester) }
                            .conditional(roofLevels == null || !roofLevels.isDigitsOnly()) { border(2.dp, color = MaterialTheme.colors.error) },
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            textAlign = TextAlign.Center
                        ),
                    )
                    Text(
                        text = stringResource(R.string.quest_buildingLevels_roofLevelsLabel2),
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.body2,
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    )
                }
            }
        }
        BuildingLevelsButtons(previousBuildingLevels, onButton)
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
@Preview(showBackground = true)
@Preview(showBackground = true, locale = "ar")
private fun BuildingLevelsFormPreview() {
    val regularLevels = remember { mutableStateOf("55") }
    val roofLevels = remember { mutableStateOf("55") }
    BuildingLevelsForm(
        levels = regularLevels.value,
        onLevels = { },
        roofLevels = roofLevels.value,
        onRoofLevels = { },
        onButton = { _, _ -> },
        previousBuildingLevels = listOf(
            BuildingLevels(5, 2),
            BuildingLevels(4, 1),
            BuildingLevels(3, 0)
        ),
    )
}
