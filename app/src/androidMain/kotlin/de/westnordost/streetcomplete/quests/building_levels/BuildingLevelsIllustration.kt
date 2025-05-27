package de.westnordost.streetcomplete.quests.building_levels

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R

/** Shows the building illustration picture and stretches the green "ground" line to the whole
 *  width of the layout. */
@Composable
fun BuildingLevelsIllustration(modifier: Modifier = Modifier) {
    Row(modifier) {
        Spacer(Modifier
            .padding(top = 136.dp)
            .weight(1f)
            .height(8.dp)
            .background(Color(0xff82a04e))
        )
        // for RTL direction, flip picture (because the whole layout is flipped, too)
        val illustrationScale = when (LocalLayoutDirection.current) {
            LayoutDirection.Ltr -> 1f
            LayoutDirection.Rtl -> -1f
        }
        Image(
            painter = painterResource(R.drawable.building_levels_illustration),
            contentDescription = "Illustration for building Levels",
            modifier = Modifier.scale(illustrationScale, 1f)
        )
        Spacer(Modifier
            .padding(top = 80.dp)
            .weight(1f)
            .height(8.dp)
            .background(Color(0xff82a04e))
        )
    }
}

@Preview
@Preview(locale = "ar")
@Composable
private fun BuildingLevelsIllustrationPreview() {
    BuildingLevelsIllustration()
}
