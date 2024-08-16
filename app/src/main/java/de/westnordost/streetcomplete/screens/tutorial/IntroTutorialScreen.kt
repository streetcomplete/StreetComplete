package de.westnordost.streetcomplete.screens.tutorial

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.main.controls.LocationState
import de.westnordost.streetcomplete.screens.main.controls.LocationStateButton
import de.westnordost.streetcomplete.ui.common.Pin
import de.westnordost.streetcomplete.ui.theme.headlineLarge
import de.westnordost.streetcomplete.ui.theme.titleLarge
import kotlinx.coroutines.launch

/** Shows a short tutorial for first-time users */
@Composable
fun IntroTutorialScreen(
    onDismissRequest: () -> Unit,
    onExplainedNeedForLocationPermission: () -> Unit = {},
    onFinished: () -> Unit = {},
    dismissOnBackPress: Boolean = false,
) {
    TutorialScreen(
        pageCount = 4,
        onDismissRequest = onDismissRequest,
        onFinished = onFinished,
        onPageChanged = { page ->
            if (page == 2) {
                onExplainedNeedForLocationPermission()
            }
        },
        illustration = { page ->
            IntroTutorialIllustration(page)
        },
        dismissOnBackPress = dismissOnBackPress,
    ) { page ->
        Column(
            modifier = Modifier.fillMaxSize(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (page) {
                0 -> IntroTutorialStep0Text()
                1 -> IntroTutorialStep1Text()
                2 -> IntroTutorialStep2Text()
                3 -> IntroTutorialStep3Text()
            }
        }
    }
}

@Composable
private fun BoxScope.IntroTutorialIllustration(
    page: Int
) {
    val mapZoom = remember { Animatable(0f) }
    val button = remember { Animatable(0f) }
    val pin1 = remember { Animatable(0f) }
    val pin2 = remember { Animatable(0f) }
    val pin3 = remember { Animatable(0f) }
    val checkmark = remember { Animatable(0f) }

    LaunchedEffect(page) {
        // map is zoomed in and magnifier is zoomed out and faded out on page > 0
        launch { mapZoom.animateTo(if (page == 0) 0f else 1f, tween(800)) }

        // show button on page 1 and 2 (appear with delay)
        if (page in 1..2) {
            launch { button.animateTo(1f, tween(400, 400)) }
        } else {
            launch { button.animateTo(0f, tween(400)) }
        }

        // drop pins on page 2
        launch { pin1.animateTo(if (page == 2) 1f else 0f, tween(400, 400)) }
        launch { pin2.animateTo(if (page == 2) 1f else 0f, tween(400, 600)) }
        launch { pin3.animateTo(if (page == 2) 1f else 0f, tween(400, 800)) }

        // checkmark on page 3
        if (page == 3) {
            checkmark.animateTo(1f, tween(1200, 1200))
        } else {
            checkmark.animateTo(0f, tween(400))
        }
    }

    Box(contentAlignment = Alignment.TopStart) {
        Box(
            Modifier
                .size(width = 226.dp, height = 222.dp)
                .graphicsLayer {
                    val scale = 1f + mapZoom.value * 0.5f
                    scaleX = scale
                    scaleY = scale
                    rotationX = mapZoom.value * 50f
                }
        ) {
            Image(
                painter = painterResource(R.drawable.logo_osm_map),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            Image(
                painter = painterResource(R.drawable.logo_osm_map_lighting),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(1f - mapZoom.value)
            )
        }

        val pinDropHeight = 200.dp
        Pin(
            iconPainter = painterResource(R.drawable.ic_quest_traffic_lights),
            modifier = Modifier
                .absolutePadding(left = 0.dp, top = 25.dp)
                .graphicsLayer {
                    alpha = pin1.value
                    translationY = -(1f - pin1.value) * pinDropHeight.toPx()
                }
        )
        Pin(
            iconPainter = painterResource(R.drawable.ic_quest_street),
            modifier = Modifier
                .absolutePadding(left = 45.dp, top = 110.dp)
                .graphicsLayer {
                    alpha = pin2.value
                    translationY = -(1f - pin2.value) * pinDropHeight.toPx()
                }
        )
        Pin(
            iconPainter = painterResource(R.drawable.ic_quest_recycling),
            modifier = Modifier
                .absolutePadding(left = 160.dp, top = 70.dp)
                .graphicsLayer {
                    alpha = pin3.value
                    translationY = -(1f - pin3.value) * pinDropHeight.toPx()
                }
        )

        LocationStateButton(
            onClick = {},
            state = if (page == 1) LocationState.SEARCHING else LocationState.UPDATING,
            modifier = Modifier
                .absoluteOffset(150.dp, 150.dp)
                .alpha(button.value)
        )

        Image(
            painter = painterResource(R.drawable.logo_osm_magnifier),
            contentDescription = null,
            modifier = Modifier
                .size(225.dp)
                .absolutePadding(left = 15.dp, top = 15.dp)
                .graphicsLayer {
                    val scale = 1f + mapZoom.value * 5f
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0.67f, 0.33f)
                    alpha = 1f - mapZoom.value
                }
        )
    }

    Image(
        painter = checkmarkCirclePainter(checkmark.value),
        contentDescription = null,
        modifier = Modifier.align(Alignment.Center)
    )
}

@Composable
private fun IntroTutorialStep0Text() {
    Text(
        text = stringResource(R.string.tutorial_welcome_to_osm),
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center
    )
    Text(
        text = stringResource(R.string.tutorial_welcome_to_osm_subtitle),
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 24.dp)
    )
}

@Composable
private fun IntroTutorialStep1Text() {
    Text(
        text = stringResource(R.string.tutorial_intro),
        style = MaterialTheme.typography.body1,
        textAlign = TextAlign.Center
    )
    Text(
        text = stringResource(R.string.no_location_permission_warning),
        style = MaterialTheme.typography.body1,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 24.dp)
    )
}

@Composable
private fun IntroTutorialStep2Text() {
    Text(
        text = stringResource(R.string.tutorial_solving_quests),
        style = MaterialTheme.typography.body1,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun IntroTutorialStep3Text() {
    Text(
        text = stringResource(R.string.tutorial_stay_safe),
        style = MaterialTheme.typography.body1,
        textAlign = TextAlign.Center,
    )
    Text(
        text = stringResource(R.string.tutorial_happy_mapping),
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 24.dp)
    )
}

@Preview(device = Devices.NEXUS_5) // darn small device
@PreviewScreenSizes
@Composable
private fun PreviewIntroTutorialScreen() {
    IntroTutorialScreen({}, {}, {})
}
