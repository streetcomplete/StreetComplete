package de.westnordost.streetcomplete.screens.tutorial

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.MapButton
import de.westnordost.streetcomplete.ui.common.Pin
import de.westnordost.streetcomplete.ui.theme.headlineSmall
import de.westnordost.streetcomplete.ui.theme.titleSmall

@Composable
fun IntroTutorialScreen(
    onFinished: () -> Unit,
) {
    TutorialScreen(
        pageCount = 3,
        onFinished = onFinished,
        illustration = { page ->
            Box(contentAlignment = Alignment.TopStart) {
                IntroTutorialIllustration(page)
            }
        }
    ) { page ->
        Column(
            modifier = Modifier.fillMaxSize(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (page) {
                0 -> IntroTutorialStep1Text()
                1 -> IntroTutorialStep2Text()
                2 -> IntroTutorialStep3Text()
            }
        }
    }
}

@Composable
private fun IntroTutorialIllustration(
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
            painter = painterResource(R.drawable.logo_osm_map_lighting),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }

    MapButton(
        onClick = {},
        modifier = Modifier.absoluteOffset(x = 200.dp, y = 130.dp)
    ) {
        Icon(painterResource(R.drawable.ic_location_no_location_24dp), null)
    }

    Pin(
        iconPainter = painterResource(R.drawable.ic_quest_recycling),
        modifier = Modifier.absoluteOffset(x = 120.dp, y = 60.dp)
    )
    Pin(
        iconPainter = painterResource(R.drawable.ic_quest_street),
        modifier = Modifier.absoluteOffset(x = 45.dp, y = 110.dp)
    )
    Pin(
        iconPainter = painterResource(R.drawable.ic_quest_traffic_lights),
        modifier = Modifier.absoluteOffset(x = 0.dp, y = 25.dp)
    )

    // TODO checkmark circle

    Image(
        painter = painterResource(R.drawable.logo_osm_magnifier),
        contentDescription = null,
        modifier = Modifier
            .size(185.dp)
            .offset(x = 20.dp, y = 16.dp)
    )
}

@Composable
private fun IntroTutorialStep1Text() {
    Text(
        text = stringResource(R.string.tutorial_welcome_to_osm),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )
    Text(
        text = stringResource(R.string.tutorial_welcome_to_osm_subtitle),
        style = MaterialTheme.typography.titleSmall,
        textAlign = TextAlign.Center
    )
    Text(
        text = stringResource(R.string.tutorial_intro),
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
    Text(
        text = stringResource(R.string.no_location_permission_warning),
        style = MaterialTheme.typography.body1,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 24.dp)
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
        style = MaterialTheme.typography.headlineSmall,
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
