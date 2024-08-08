package de.westnordost.streetcomplete.screens.main.teammode

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.tutorial.TutorialScreen
import de.westnordost.streetcomplete.ui.common.WheelPicker
import de.westnordost.streetcomplete.ui.common.WheelPickerState
import de.westnordost.streetcomplete.ui.common.rememberWheelPickerState
import de.westnordost.streetcomplete.ui.theme.TeamColors
import de.westnordost.streetcomplete.ui.theme.headlineLarge
import de.westnordost.streetcomplete.ui.theme.titleLarge

// TODO disable next button when not input anything yet....

/** Wizard which enables team mode */
@Composable
fun TeamModeWizard(
    onDismissRequest: () -> Unit,
    onFinished: (teamSize: Int, indexInTeam: Int) -> Unit,
) {
    val teamSizes = remember { (2..TeamColors.size).toList() }
    val teamSizeState = rememberWheelPickerState()
    var indexInTeam by remember { mutableStateOf<Int?>(null) }
    val teamSize = teamSizes[teamSizeState.selectedItemIndex]

    TutorialScreen(
        pageCount = 3,
        onDismissRequest = onDismissRequest,
        onFinished = { onFinished(teamSize, indexInTeam!!) },
        illustration = { page ->
            Image(painterResource(R.drawable.team_mode), null)
        }
    ) { page ->
        Column(
            modifier = Modifier.fillMaxSize(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (page) {
                0 -> TeamModeDescription()
                1 -> TeamModeTeamSizeInput(
                    teamSizes = teamSizes,
                    teamSizeState = teamSizeState
                )
                2 -> TeamModeColorSelect(
                    teamSize = teamSize,
                    selectedIndex = indexInTeam,
                    onSelectedIndex = { indexInTeam = it }
                )
            }
        }
    }
}

@Composable
private fun TeamModeDescription() {
    Text(
        text = stringResource(R.string.team_mode),
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center,
    )
    Text(
        text = stringResource(R.string.team_mode_description),
        style = MaterialTheme.typography.body1,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 24.dp)
    )
}

@Composable
private fun TeamModeTeamSizeInput(
    teamSizes: List<Int>,
    teamSizeState: WheelPickerState
) {
    Text(
        text = stringResource(R.string.team_mode_team_size_label2),
        style = MaterialTheme.typography.body1,
        textAlign = TextAlign.Center
    )
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.headlineLarge) {
        WheelPicker(
            items = teamSizes,
            modifier = Modifier.padding(top = 24.dp).width(96.dp),
            visibleAdjacentItems = 1,
            state = teamSizeState,
        ) {
            Text(it.toString())
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TeamModeColorSelect(
    teamSize: Int,
    selectedIndex: Int?,
    onSelectedIndex: (Int) -> Unit,
) {
    Text(
        text = stringResource(R.string.team_mode_choose_color2),
        style = MaterialTheme.typography.body1,
        textAlign = TextAlign.Center
    )
    FlowRow(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = 24.dp)
    ) {
        for (index in 0..<teamSize) {
            TeamModeColorCircle(
                index = index,
                modifier = Modifier
                    .clickable { onSelectedIndex(index) }
                    .padding(4.dp)
                    .width(64.dp)
            )
        }
    }
}

@Preview
@Composable
private fun PreviewTeamModeWizard() {
    TeamModeWizard(
        onDismissRequest = { },
        onFinished = { _, _ -> }
    )
}
