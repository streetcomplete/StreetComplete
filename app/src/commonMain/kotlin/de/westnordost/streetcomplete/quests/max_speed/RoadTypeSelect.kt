package de.westnordost.streetcomplete.quests.max_speed

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.item_select.ItemSelectGrid
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Form in which to select the default max speed road type. This is usually just a binary
 *  selection between urban or rural. There is a special case for United Kingdom due to historic
 *  reasons. */
@Composable
fun RoadTypeSelect(
    roadType: RoadType?,
    onRoadType: (RoadType?) -> Unit,
    countryCode: String,
    modifier: Modifier = Modifier,
) {
    val selectable = remember(countryCode) { RoadType.getEntriesByCountryCode(countryCode)  }
    val cells = selectable.size.coerceIn(1, 3)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(Res.string.quest_maxspeed_answer_roadtype_description),
            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
            style = MaterialTheme.typography.body2,
        )
        ItemSelectGrid(
            columns = SimpleGridCells.Fixed(cells),
            items = selectable,
            selectedItem = roadType,
            onSelect = onRoadType,
        ) {
            ImageWithLabel(
                painter = painterResource(it.icon),
                label = stringResource(it.text)
            )
        }
    }
}
