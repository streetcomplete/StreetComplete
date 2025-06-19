package de.westnordost.streetcomplete.quests.max_height

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun MaxHeightSignRound(
    resourceId: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .width(240.dp) // TODO Hardcoded size to keep the LengthInput inside the sign
            .height(240.dp)
    ) {
        Image(
            painter = painterResource(resourceId),
            contentDescription = "Maximum Height Sign",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        )
        Box(modifier = Modifier.align(Alignment.Center)) {
            content()
        }
    }
}
