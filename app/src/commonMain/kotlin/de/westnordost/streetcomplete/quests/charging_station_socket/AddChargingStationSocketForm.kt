package de.westnordost.streetcomplete.quests.charging_station_socket

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddChargingStationSocketForm(
    on: (QuestAction<Map<SocketType, Int>>) -> Unit,
    element: Element,
    socketTypes: List<SocketType>,
    domesticIcons: List<DrawableResource> = emptyList(),
) {
    val initialCounts = remember(element.id, socketTypes) {
        initialSocketCounts(element.tags, socketTypes).mapValues { it.value as Int? }
    }
    var counts by remember(element.id, socketTypes) { mutableStateOf(initialCounts) }

    QuestForm(
        on = on,
        isComplete = counts.values.any { (it ?: 0) > 0 },
        hasChanges = counts.mapValues { it.value ?: 0 } !=
            initialCounts.mapValues { it.value ?: 0 },
        onClickOk = {
            on(Answer(counts.mapValues { (_, count) -> count ?: 0 }))
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.medium,
                LocalTextStyle provides MaterialTheme.typography.body2
            ) {
                Text(stringResource(Res.string.quest_charging_station_socket_note))
            }
            SocketTypeAndCountForm(
                socketTypes = socketTypes,
                counts = counts,
                onCountsChanged = { counts = it },
                domesticIcons = domesticIcons,
            )
        }
    }
}
