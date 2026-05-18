package de.westnordost.streetcomplete.quests.railway_crossing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddRailwayCrossingBarrierForm(
    onAnswer: (RailwayCrossingBarrier) -> Unit,
    element: Element,
    countryInfo: CountryInfo,
) {
    val items = remember {
        val isPedestrian = element.tags["railway"] == "crossing"
        RailwayCrossingBarrier.getSelectableValues(isPedestrian)
    }

    ItemSelectQuestForm(
        items = items,
        itemsPerRow = 2,
        itemContent = { item ->
            ImageWithLabel(
                painter = painterResource(item.getIcon(countryInfo.isLeftHandTraffic)),
                label = item.title?.let { stringResource(it) }
            )
        },
        onClickOk = onAnswer,
    )
}
