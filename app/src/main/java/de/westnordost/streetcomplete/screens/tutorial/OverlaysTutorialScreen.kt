package de.westnordost.streetcomplete.screens.tutorial

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.user.achievements.AnimatedTadaShine
import de.westnordost.streetcomplete.ui.common.MapButton
import de.westnordost.streetcomplete.ui.theme.headlineSmall

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OverlaysTutorialScreen(
    onFinished: () -> Unit,
) {
    TutorialScreen(
        pageCount = 3,
        onFinished = onFinished,
        illustration = { page ->
            Box(contentAlignment = Alignment.TopStart) {
                OverlaysTutorialIllustration(page)
            }
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

@Composable
private fun OverlaysTutorialStepIntroText() {
    Text(
        text = stringResource(R.string.overlays_tutorial_title),
        style = MaterialTheme.typography.headlineSmall,
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

@Composable
private fun OverlaysTutorialIllustration(
    page: Float
) {
    // TODO animate steps

    Box(Modifier.size(width = 226.dp, height = 222.dp)) {
        Image(
            painter = painterResource(R.drawable.logo_osm_map),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(R.drawable.overlay_osm_map),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        Image(
            painter = painterResource(R.drawable.ic_preset_fas_shopping_cart),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .absoluteOffset(80.dp, 40.dp)
        )
        Image(
            painter = painterResource(R.drawable.ic_preset_maki_fuel),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .absoluteOffset(180.dp, 170.dp)
        )
        Image(
            painter = painterResource(R.drawable.overlay_osm_map_edit),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }

    Image(
        painter = painterResource(R.drawable.paint_roller),
        contentDescription = null,
        modifier = Modifier.rotate(-45f)
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .absoluteOffset(x = 228.dp, y = 158.dp),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedTadaShine()

        MapButton(onClick = {},) {
            Icon(painterResource(R.drawable.ic_overlay_black_24dp), null)
        }
    }

}

@Preview
@Composable
private fun PreviewOverlaysTutorialIllustration() {
    OverlaysTutorialIllustration(page = 0f)
}

@Preview
@Composable
private fun PreviewOverlaysTutorialScreen() {
    OverlaysTutorialScreen {}
}
