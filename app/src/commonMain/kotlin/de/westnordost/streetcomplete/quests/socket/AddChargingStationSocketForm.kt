package de.westnordost.streetcomplete.quests.socket

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.allDrawableResources
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.ktx.isInEu

@Composable
fun AddChargingStationSocketForm(
    on: (QuestAction<Map<ChargingStationSocket, Int>>) -> Unit,
    element: Element,
    countryInfo: CountryInfo,
) {
    val defaultSockets = remember(countryInfo) {
        countryInfo.chargingStationSocketTypes
            .mapNotNull { ChargingStationSocket.of(it) }
            .associateWith { null }
    }
    val domesticSocketIcon = remember(countryInfo) {
        countryInfo.domesticSocketType.firstNotNullOfOrNull {
            Res.allDrawableResources["socket_domestic_"+it]
        }
    }
    val showEuLabels = remember(countryInfo) { countryInfo.isInEu }

    val initialSockets = remember(element) {
        val sockets = parseChargingStationSockets(element.tags)
        if (sockets.isNotEmpty()) sockets else defaultSockets
    }

    var sockets by rememberSerializable(initialSockets) { mutableStateOf(initialSockets) }

    QuestForm(
        on = on,
        isComplete = sockets.isNotEmpty() && sockets.values.all { it != null },
        hasChanges = sockets != initialSockets,
        isResurvey = initialSockets.isNotEmpty() && initialSockets.values.all { it != null },
        onClickOk = { on(Answer(sockets.mapValues { (_, count) -> count!! })) },
    ) {
        ChargingStationSocketsForm(
            sockets = sockets,
            onSocketsChanged = { sockets = it },
            domesticSocketIcon = domesticSocketIcon,
            showEuLabels = showEuLabels
        )
    }
}
