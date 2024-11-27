package de.westnordost.streetcomplete.quests.building_levels

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.theme.AppTheme

@Composable
fun AddBuildingLevelsButton(lastLevels: Int, lastRoofLevels: Int?,modifier: Modifier = Modifier){
    Row(modifier = modifier.padding(5.dp)) {
        Row(modifier = Modifier
            .height(52.dp)
            .wrapContentWidth()
            .widthIn(min=42.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color.LightGray)
            .padding(3.dp)
            , verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = lastLevels.toString(),
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Bottom)
            )
            Image(
                painterResource(R.drawable.ic_building_levels_illustration),
                "Building Illustration",
                modifier = Modifier.wrapContentSize()
            )
            Text(
                text = lastRoofLevels?.toString() ?: " ",
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Top)
            )
        }
    }
}


@Composable
@Preview(showBackground = true,
    name = "Add Building Levels Button"
)
fun PreviewAddBuildingLevelsButton(){
    AppTheme {
        AddBuildingLevelsButton(3, 1)
    }
}
