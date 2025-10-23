package de.westnordost.streetcomplete.quests.cycleway

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.bicycle_boulevard
import de.westnordost.streetcomplete.resources.ok
import de.westnordost.streetcomplete.resources.sign_icon_bicycle
import de.westnordost.streetcomplete.resources.sign_icon_pedestrian_and_bicycle
import de.westnordost.streetcomplete.ui.common.RectangularSign
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.ui.theme.titleLarge
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable fun BicycleInPedestrianStreetAllowedSign(modifier: Modifier = Modifier) {
    RectangularSign(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp),
        ) {
            Icon(painterResource(Res.drawable.sign_icon_bicycle), null)
            Text(
                text = stringResource(Res.string.ok),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable fun BicycleInPedestrianStreetDesignatedSign(modifier: Modifier = Modifier) {
    RectangularSign(
        modifier = modifier,
        color = TrafficSignColor.Blue,
    ) {
        Icon(
            painter = painterResource(Res.drawable.sign_icon_pedestrian_and_bicycle),
            contentDescription = null,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable fun BicycleBoulevardSign(modifier: Modifier = Modifier, ) {
    RectangularSign(
        modifier = modifier,
        color = TrafficSignColor.Blue
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp),
        ) {
            Icon(painterResource(Res.drawable.sign_icon_bicycle), null)
            Text(
                text = stringResource(Res.string.bicycle_boulevard),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
