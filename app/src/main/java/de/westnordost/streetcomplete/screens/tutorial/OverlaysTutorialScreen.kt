package de.westnordost.streetcomplete.screens.tutorial

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.main.controls.MapButton
import de.westnordost.streetcomplete.screens.user.achievements.AnimatedTadaShine
import de.westnordost.streetcomplete.ui.theme.headlineLarge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OverlaysTutorialScreen(
    onDismissRequest: () -> Unit,
    onFinished: () -> Unit = {},
    dismissOnBackPress: Boolean = true,
) {
    TutorialScreen(
        pageCount = 3,
        onDismissRequest = onDismissRequest,
        onFinished = onFinished,
        dismissOnBackPress = dismissOnBackPress,
        illustration = { page ->
            OverlaysTutorialIllustration(page)
        },
    ) { page ->
        Column(
            modifier = Modifier.fillMaxSize(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (page) {
                0 -> OverlaysTutorialStepIntroText()
                1 -> OverlaysTutorialStepDisplayText()
                2 -> OverlaysTutorialStepEditText()
            }
        }
    }
}

private enum class ShowEdit { No, Selected, Done }

@Composable
private fun BoxScope.OverlaysTutorialIllustration(
    page: Int
) {
    val density = LocalDensity.current.density
    val paintRollerPosition = remember { Animatable(0f) }
    val paintRollerAlpha = remember { Animatable(0f) }
    val overlayPaint = remember { Animatable(0f) }
    val button = remember { Animatable(0f) }
    val mapZoom = remember { Animatable(0f) }
    var showSelection by remember { mutableStateOf(ShowEdit.No) }
    val showEdit = remember { Animatable(0f) }

    var oldPage by remember { mutableIntStateOf(page) }

    LaunchedEffect(page) {
        // button only visible on page 0
        launch { button.animateTo(if (page == 0) 0f else 1f, tween(450)) }

        // paint roller rolls from the top left to the bottom right
        if (page == 1 && oldPage < 1) {
            launch {
                paintRollerAlpha.animateTo(1f, tween(300, 300))
                paintRollerAlpha.animateTo(0f, tween(300, 450))
            }
            launch { paintRollerPosition.animateTo(1f, tween(900, 300, LinearEasing)) }
        } else if (oldPage == 1 && page < 1) {
            // reverse paint-roller when going back
            launch {
                paintRollerAlpha.animateTo(1f, tween(300, 0))
                paintRollerAlpha.animateTo(0f, tween(300, 450))
            }
            paintRollerPosition.snapTo(1f)
            launch { paintRollerPosition.animateTo(0f, tween(900, 150, LinearEasing)) }
        } else {
            launch { paintRollerAlpha.animateTo(0f, tween(300)) }
            launch { paintRollerPosition.animateTo(0f, tween(300, 300)) }
        }
        // overlay paint is visible on pages > 0
        if (page > 0) {
            launch { overlayPaint.animateTo(1f, tween(900, 600, LinearEasing)) }
        } else {
            launch { overlayPaint.animateTo(0f, tween(900)) }
        }
        // map zooms in on page 2, shows selection etc.
        if (page == 2) {
            showSelection = ShowEdit.Selected
            mapZoom.animateTo(1f, tween(900))
            delay(3600)
            showSelection = ShowEdit.Done
            showEdit.animateTo(1f, tween(450))
        } else {
            showSelection = ShowEdit.No
            showEdit.animateTo(0f, tween(300))
            mapZoom.animateTo(0f, tween(900))
        }
        oldPage = page
    }

    Box(contentAlignment = Alignment.TopStart) {
        Box(
            Modifier
                .size(width = 226.dp, height = 222.dp)
                .graphicsLayer {
                    val scale = 1f + mapZoom.value
                    scaleX = scale
                    scaleY = scale
                    rotationZ = mapZoom.value * -15f
                    transformOrigin = TransformOrigin(0.25f, 0.75f)
                    translationX = 45f * mapZoom.value * density
                }
        ) {
            Image(
                painter = painterResource(R.drawable.logo_osm_map),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            Image(
                painter = overlayPainter(overlayPaint.value),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            if (overlayPaint.value > 0.2f) {
                Icon(
                    painter = painterResource(R.drawable.ic_preset_fas_shopping_cart),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier
                        .size(24.dp)
                        .absoluteOffset(80.dp, 40.dp)
                )
            }
            if (overlayPaint.value > 0.7f) {
                Icon(
                    painter = painterResource(R.drawable.ic_preset_maki_fuel),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier
                        .size(24.dp)
                        .absoluteOffset(180.dp, 170.dp)
                )
            }

            if (showSelection == ShowEdit.Selected) {
                val highlightTransition = rememberInfiniteTransition()
                val highlight by highlightTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(1200, 0, LinearEasing))
                )
                Image(
                    painter = overlayEditHighlightedPainter(highlight),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (showSelection == ShowEdit.Done) {
                Image(
                    painter = overlayEditDonePainter(showEdit.value),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        val paintRollingTransition = rememberInfiniteTransition()
        val paintRolling by paintRollingTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(600, 0, LinearEasing))
        )
        Image(
            painter = paintRollerPainter(paintRolling),
            contentDescription = null,
            modifier = Modifier.graphicsLayer {
                val offset = (-128 + paintRollerPosition.value * 256) * density
                translationX = offset
                translationY = offset
                rotationZ = -45.0f
                alpha = paintRollerAlpha.value
            }
        )

        Box(
            modifier = Modifier
                .size(56.dp)
                .absoluteOffset(150.dp, 150.dp)
                .alpha(1f - button.value),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedTadaShine()
            MapButton(onClick = {}) {
                Icon(
                    painter = painterResource(R.drawable.ic_overlay_black_24dp),
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun OverlaysTutorialStepIntroText() {
    Text(
        text = stringResource(R.string.overlays_tutorial_title),
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center,
    )
    Text(
        text = stringResource(R.string.overlays_tutorial_intro),
        style = MaterialTheme.typography.body1,
        modifier = Modifier.padding(top = 24.dp),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun OverlaysTutorialStepDisplayText() {
    Text(
        text = stringResource(R.string.overlays_tutorial_display),
        style = MaterialTheme.typography.body1,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun OverlaysTutorialStepEditText() {
    Text(
        text = stringResource(R.string.overlays_tutorial_edit),
        style = MaterialTheme.typography.body1,
        textAlign = TextAlign.Center,
    )
}

@PreviewScreenSizes
@Composable
private fun PreviewOverlaysTutorialScreen() {
    OverlaysTutorialScreen({}, {})
}
