package de.westnordost.streetcomplete.quests.socket

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
        initialSocketFormCounts(element.tags, socketTypes)
    }
    var counts by remember(element.id, socketTypes) { mutableStateOf(initialCounts) }
    // Resurvey bubble only when every displayed type was already numeric/`no`
    // (not for first survey, bare `yes`, or missing displayed keys).
    val isResurvey = remember(element.id, element.tags, socketTypes) {
        isCompleteSocketSurvey(element.tags, socketTypes)
    }
    val unresolvedYesTypes = remember(element.id, element.tags, socketTypes) {
        socketTypes.filter { parseSocketPresence(element.tags[it.osmCountKey]) is SocketPresence.Yes }
            .toSet()
    }

    QuestForm(
        on = on,
        // Non-empty form and every displayed type resolved (0 = explicit no, >0 = count).
        isComplete = isSocketFormComplete(counts, socketTypes),
        hasChanges = counts != initialCounts,
        isResurvey = isResurvey,
        onClickOk = {
            on(Answer(counts.mapValues { (_, count) -> count!! }))
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
                unresolvedYesTypes = unresolvedYesTypes,
                domesticIcons = domesticIcons,
            )
        }
    }
}
