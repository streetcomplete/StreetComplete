package de.westnordost.streetcomplete.screens.main.teammode

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.tutorial.TutorialScreen
import de.westnordost.streetcomplete.ui.theme.TeamColors
import de.westnordost.streetcomplete.ui.theme.headlineLarge

// TODO disable next button

/** Wizard which enables team mode */
@Composable
fun TeamModeWizard(
    onDismissRequest: () -> Unit,
    onFinished: (teamSize: Int, indexInTeam: Int) -> Unit,
) {
    var teamSize by remember { mutableStateOf(TextFieldValue()) }
    var indexInTeam by remember { mutableStateOf<Int?>(null) }

    fun isValidTeamSize(input: String): Boolean {
        val size = input.toIntOrNull()
        return size != null && size in 2..12
    }

    TutorialScreen(
        pageCount = 3,
        onDismissRequest = onDismissRequest,
        onFinished = { onFinished(teamSize.text.toInt(), indexInTeam!!) },
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
                    value = teamSize,
                    onValueChange = { if (isValidTeamSize(it.text)) teamSize = it }
                )
                2 -> TeamModeColorSelect(
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
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(value) { focusRequester.requestFocus() }

    Text(
        text = stringResource(R.string.team_mode_team_size_label2),
        style = MaterialTheme.typography.body1,
        textAlign = TextAlign.Center
    )
    // TODO number picker
}

@Composable
private fun TeamModeColorSelect(
    selectedIndex: Int?,
    onSelectedIndex: (Int) -> Unit,
) {
    Text(
        text = stringResource(R.string.team_mode_choose_color2),
        style = MaterialTheme.typography.body1,
        textAlign = TextAlign.Center
    )
    LazyVerticalGrid(
        columns = GridCells.Adaptive(52.dp),
        modifier = Modifier.padding(top = 24.dp)
    ) {
        items(TeamColors.size) { index ->
            TeamModeColorCircle(
                index = index,
                modifier = Modifier
                    .clickable { onSelectedIndex(index) }
                    .padding(4.dp)
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
