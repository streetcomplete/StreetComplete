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
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddCyclewaySegregationForm : AbstractOsmQuestForm<CyclewaySegregation>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
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
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            favoriteKey = "AddCyclewaySegregationForm",
        )
    }
}
