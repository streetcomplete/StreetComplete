package de.westnordost.streetcomplete.screens.tutorial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
    onFinished: () -> Unit,
) {
    TutorialScreen(
        pageCount = 4,
        onFinished = onFinished,
        illustration = { page ->
            IntroTutorialIllustration(page)
        }
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
    val mapTilt = remember { Animatable(0f) }
    val mapScale = remember { Animatable(1f) }
    val mapShine = remember { Animatable(1f) }

    LaunchedEffect(page) {
        val mapSpec = tween<Float>(800)
        launch { mapTilt.animateTo(if (page == 0) 0f else 50f, mapSpec) }
        launch { mapScale.animateTo(if (page == 0) 1f else 1.5f, mapSpec) }
        launch { mapShine.animateTo(if (page == 0) 1f else 0f, mapSpec) }
    }

    Box(contentAlignment = Alignment.TopStart) {
        Box(Modifier.size(width = 226.dp, height = 222.dp)) {
            Image(
                painter = painterResource(R.drawable.logo_osm_map),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationX = mapTilt.value
                        scaleX = mapScale.value
                        scaleY = mapScale.value
                    }
            )
            Image(
                painter = painterResource(R.drawable.logo_osm_map_lighting),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationX = mapTilt.value
                        scaleX = mapScale.value
                        scaleY = mapScale.value
                        alpha = mapShine.value
                    }
            )
        }

        AnimatedVisibility(
            visible = page == 2,
            enter = slideInVertically(tween(400, 400)) + fadeIn(tween(400, 400)),
            exit = fadeOut(tween(400))
        ) {
            Pin(
                iconPainter = painterResource(R.drawable.ic_quest_traffic_lights),
                modifier = Modifier.absolutePadding(left = 0.dp, top = 25.dp)
            )
        }
        AnimatedVisibility(
            visible = page == 2,
            enter = slideInVertically(tween(400, 600)) + fadeIn(tween(400, 600)),
            exit = fadeOut(tween(400))
        ) {
            Pin(
                iconPainter = painterResource(R.drawable.ic_quest_street),
                modifier = Modifier.absolutePadding(left = 45.dp, top = 110.dp)
            )
        }
        AnimatedVisibility(
            visible = page == 2,
            enter = slideInVertically(tween(400, 800)) + fadeIn(tween(400, 800)),
            exit = fadeOut(tween(400))
        ) {
            Pin(
                iconPainter = painterResource(R.drawable.ic_quest_recycling),
                modifier = Modifier.absolutePadding(left = 160.dp, top = 70.dp)
            )
        }

        val zoomScale = 6f
        val magnifierOrigin = TransformOrigin(0.67f, 0.33f)
        val magnifierAnimSpec = tween<Float>(800)
        AnimatedVisibility(
            visible = page == 0,
            exit =
                scaleOut(magnifierAnimSpec, zoomScale, magnifierOrigin) +
                fadeOut(magnifierAnimSpec),
            enter =
                scaleIn(magnifierAnimSpec, zoomScale, magnifierOrigin) +
                fadeIn(magnifierAnimSpec)
        ) {
            Image(
                painter = painterResource(R.drawable.logo_osm_magnifier),
                contentDescription = null,
                modifier = Modifier
                    .size(225.dp)
                    .absolutePadding(left = 15.dp, top = 15.dp)
            )
        }
    }

    AnimatedVisibility(
        visible = page in 1..2,
        enter = fadeIn(tween(400, 400)),
        exit = fadeOut(tween(400)),
        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
    ) {
        LocationStateButton(
            onClick = {},
            state = if (page == 1) LocationState.SEARCHING else LocationState.UPDATING,
        )
    }

    if (page == 3) {
        val checkmarkProgress = remember { Animatable(0f) }
        LaunchedEffect(page) {
            checkmarkProgress.animateTo(1f, tween(800, 400, LinearEasing))
        }
        Image(
            painter = checkmarkCircle(progress = checkmarkProgress.value),
            contentDescription = null,
            modifier = Modifier.align(Alignment.Center)
        )
    }
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
    IntroTutorialScreen {}
}
