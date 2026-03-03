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
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddCyclewaySegregationForm : AItemSelectQuestForm<CyclewaySegregation, CyclewaySegregation>() {

    override val items = CyclewaySegregation.entries
    override val itemsPerRow = 1
    override val serializer = serializer<CyclewaySegregation>()

    @Composable override fun ItemContent(item: CyclewaySegregation) {
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
    }

    override fun onClickOk(selectedItem: CyclewaySegregation) {
        applyAnswer(selectedItem)
    }
}
