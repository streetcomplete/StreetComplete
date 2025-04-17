package de.westnordost.streetcomplete.screens.main.teammode

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.tutorial.TutorialScreen
import de.westnordost.streetcomplete.ui.common.BubblePile
import de.westnordost.streetcomplete.ui.common.WheelPicker
import de.westnordost.streetcomplete.ui.common.WheelPickerState
import de.westnordost.streetcomplete.ui.common.rememberWheelPickerState
import de.westnordost.streetcomplete.ui.ktx.conditional
import de.westnordost.streetcomplete.ui.ktx.toPx
import de.westnordost.streetcomplete.ui.theme.TeamColors
import de.westnordost.streetcomplete.ui.theme.headlineLarge
import de.westnordost.streetcomplete.ui.theme.selectionBackground
import kotlinx.coroutines.delay

/** Wizard which enables team mode */
@Composable
fun TeamModeWizard(
    onDismissRequest: () -> Unit,
    onFinished: (teamSize: Int, indexInTeam: Int) -> Unit,
    allQuestIconIds: List<Int>,
) {
    val teamSizes = remember { (2..TeamColors.size).toList() }
    val teamSizeState = rememberWheelPickerState()
    var indexInTeam by remember { mutableIntStateOf(-1) }
    val teamSize = teamSizes[teamSizeState.selectedItemIndex]

    TutorialScreen(
        pageCount = 3,
        onDismissRequest = onDismissRequest,
        onFinished = { onFinished(teamSize, indexInTeam) },
        dismissOnBackPress = true,
        nextIsEnabled = { page ->
            if (page == 2 && indexInTeam !in 0..<teamSize) false
            else true
        },
        illustration = { page ->
            val selectedIndex = if (page > 1) indexInTeam else -1
            AnimatedContent(
                targetState = page > 0,
                transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(600)) }
            ) {
                when (it) {
                    false -> SplitQuestsIllustration(allQuestIconIds = allQuestIconIds)
                    true -> TeamSizeIllustration(teamSize = teamSize, selectedIndex = selectedIndex)
                }
            }
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
    Text(
        text = stringResource(R.string.team_mode_description_overlay_hint),
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
            modifier = Modifier
                .padding(top = 24.dp)
                .width(96.dp),
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
    selectedIndex: Int,
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
                    .conditional(selectedIndex == index) {
                        background(
                            color = MaterialTheme.colors.selectionBackground,
                            shape = MaterialTheme.shapes.small
                        )
                    }
                    .padding(8.dp)
                    .width(56.dp)
            )
        }
    }
}

@Composable
private fun SplitQuestsIllustration(
    allQuestIconIds: List<Int>
) {
    val padding = remember { Animatable(0f) }
    val divider = remember { Animatable(0f) }
    LaunchedEffect(allQuestIconIds) {
        delay(1000)
        padding.animateTo(1f, tween(1000))
        divider.animateTo(1f, tween(500))
    }

    val arrangement = Arrangement.spacedBy((-48 + 64 * padding.value).dp)
    val dividerColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
    val dividerWidth = 4.dp.toPx()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawLine(
                    color = dividerColor,
                    start = Offset(center.x, 0f),
                    end = Offset(center.x, size.height * divider.value),
                    strokeWidth = dividerWidth,
                )
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, center.y),
                    end = Offset(size.width * divider.value, center.y),
                    strokeWidth = dividerWidth,
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = arrangement
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = arrangement
        ) {
            QuestPile(allQuestIconIds, Modifier.weight(1f))
            QuestPile(allQuestIconIds, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = arrangement
        ) {
            QuestPile(allQuestIconIds, Modifier.weight(1f))
            QuestPile(allQuestIconIds, Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuestPile(
    allQuestIconIds: List<Int>,
    modifier: Modifier = Modifier,
) {
    BubblePile(
        count = 15,
        allIconsIds = allQuestIconIds,
        bubbleSize = 50.dp,
        modifier = modifier
    )
}

@Composable
private fun TeamSizeIllustration(teamSize: Int, selectedIndex: Int) {
    for (i in hands.indices) {
        val hasSelection = selectedIndex >= 0
        val isSelected = selectedIndex == i
        val animatedSelected by animateFloatAsState(if (isSelected) 1f else 0f)

        AnimatedVisibility(
            visible = i < teamSize,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(if (isSelected) 1f else 0f)
                .scale(1f + animatedSelected * 0.5f),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val colorFilter = if (hasSelection) ColorFilter.saturation(animatedSelected) else null
            Image(
                painter = painterResource(hands[i]),
                contentDescription = null,
                colorFilter = colorFilter,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun ColorFilter.Companion.saturation(sat: Float) =
    colorMatrix(ColorMatrix().also { it.setToSaturation(sat) })

private val hands = listOf(
    R.drawable.team_size_1,
    R.drawable.team_size_2,
    R.drawable.team_size_3,
    R.drawable.team_size_4,
    R.drawable.team_size_5,
    R.drawable.team_size_6,
    R.drawable.team_size_7,
    R.drawable.team_size_8,
    R.drawable.team_size_9,
    R.drawable.team_size_10,
    R.drawable.team_size_11,
    R.drawable.team_size_12,
)

@Preview
@Composable
private fun PreviewTeamModeWizard() {
    TeamModeWizard(
        onDismissRequest = { },
        onFinished = { _, _ -> },
        allQuestIconIds = listOf(
            R.drawable.ic_quest_bicycle_parking,
            R.drawable.ic_quest_building,
            R.drawable.ic_quest_drinking_water,
            R.drawable.ic_quest_notes,
            R.drawable.ic_quest_street_surface,
            R.drawable.ic_quest_wheelchair,
        )
    )
}
