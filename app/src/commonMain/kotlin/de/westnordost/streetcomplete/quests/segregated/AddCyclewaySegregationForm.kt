package de.westnordost.streetcomplete.quests.segregated

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddCyclewaySegregationForm(
    on: (QuestAction<CyclewaySegregation>) -> Unit,
    countryInfo: CountryInfo,
) {
    ItemSelectQuestForm(
        items = CyclewaySegregation.entries,
        itemsPerRow = 1,
        itemContent = { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Image(painterResource(item.getIcon(countryInfo.isLeftHandTraffic)), null)
                Text(
                    text = stringResource(item.title),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.weight(1f).padding(4.dp)
                )
            }
        },
        on = on,
    )
}
