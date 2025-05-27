package de.westnordost.streetcomplete.quests.bike_parking_capacity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.CountInput

@Composable
fun BikeParkingCapacityForm(count: Int, onCountChange: (Int) -> Unit) {
    CountInput(count = count, onCountChange = onCountChange, iconPainter = painterResource(R.drawable.ic_bicycle))
}

@Composable
@Preview(
    name = "Bike Parking Capacity Form",
    showBackground = true,
    widthDp = 360,
    heightDp = 100
)
private fun StepCountFormPreview() {
    val count = remember { mutableIntStateOf(1) }
    BikeParkingCapacityForm(count = count.intValue, onCountChange = { count.intValue = it })
}
