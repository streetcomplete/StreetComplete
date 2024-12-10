package de.westnordost.streetcomplete.quests.building_levels

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.ktx.conditional
import de.westnordost.streetcomplete.ui.theme.AppTheme
import de.westnordost.streetcomplete.ui.theme.titleLarge

@Composable
fun AddBuildingLevelsFormControl(
    regularLevels: String?,
    onRegularLevels: (String) -> Unit,
    roofLevels: String?,
    onRoofLevels: (String) -> Unit,
    onButton: (Int, Int?) -> Unit,
    modifier: Modifier = Modifier,
    buildingLevels: List<BuildingLevelsAnswer> = listOf(),
) {
    val focusRequester = remember { FocusRequester() }

    Box(modifier = modifier) {
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier
                    .padding(3.dp)
                    .weight(1f)) {
                    Text(
                        stringResource(R.string.quest_buildingLevels_levelsLabel2),
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(0.dp, 12.dp),
                    )
                    TextField(
                        regularLevels ?: "",
                        onValueChange = onRegularLevels,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .padding(vertical = 9.dp)
                            .conditional(regularLevels == null) { focusRequester(focusRequester) }
                            .conditional(regularLevels == null || !regularLevels.isDigitsOnly()) { border(2.dp, color = MaterialTheme.colors.error) },
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            textAlign = TextAlign.Start,
                        ),

                        )
                }
                Image(
                    painter = painterResource(R.drawable.building_levels_illustration),
                    contentDescription = "Illustration for building Levels",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .defaultMinSize()
                        .width(239.dp)
                        .weight(2f)
                        .padding(3.dp)
                )
                Column(modifier = Modifier
                    .padding(3.dp)
                    .weight(1f)) {
                    TextField(
                        roofLevels ?: "",
                        onValueChange = onRoofLevels,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        modifier = Modifier
                            .padding(0.dp, 12.dp)
                            .align(Alignment.CenterHorizontally)
                            .conditional(regularLevels != null) { focusRequester(focusRequester) }
                            .conditional(roofLevels == null || !roofLevels.isDigitsOnly()) { border(2.dp, color = MaterialTheme.colors.error) },
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            textAlign = TextAlign.Start
                        )
                    )
                    Text(
                        text = stringResource(R.string.quest_buildingLevels_roofLevelsLabel2),
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(0.dp, 12.dp)
                    )
                }
            }
            AddBuildingLevelsSavedButtons(buildingLevels, onButton)
        }
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
@Preview(showBackground = true, name = "Add Building Levels Form Component")
fun PreviewAddBuildingLevelsFormControl() {
    var regularLevels = remember { mutableStateOf("55") }
    var roofLevels = remember { mutableStateOf("55") }
    AppTheme {
        AddBuildingLevelsFormControl(
            regularLevels.value,
            { },
            roofLevels.value,
            { },
            onButton = { reg, roof -> },
            buildingLevels = listOf(
                BuildingLevelsAnswer(5, 2),
                BuildingLevelsAnswer(4, 1),
                BuildingLevelsAnswer(3, 0)
            )
        )
    }
}
