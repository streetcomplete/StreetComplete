package de.westnordost.streetcomplete.quests.oneway

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.ui.common.dialogs.AreYouSureDialog
import org.jetbrains.compose.resources.stringResource
import de.westnordost.streetcomplete.resources.*

private val RARE_BOTHWAY_AERIALWAYS = arrayOf("t-bar", "j-bar", "platter", "drag_lift")

@Composable
fun AddOnewayAerialwayForm(
    on: (QuestAction<OnewayAnswer>) -> Unit,
    element: Element,
    geometry: ElementGeometry
) {
    var pendingAnswer by remember { mutableStateOf<QuestAction<OnewayAnswer>?>(null) }

    AddOnewayForm(
        on = { action ->
            if (action is Answer && action.value == OnewayAnswer.NO_ONEWAY && element.tags["aerialway"] in RARE_BOTHWAY_AERIALWAYS) {
                pendingAnswer = action
            } else {
                on(action)
            }
        },
        geometry = geometry
    )

    pendingAnswer?.let { answer ->
        AreYouSureDialog(
            onDismissRequest = { pendingAnswer = null },
            onConfirmed = { on(answer) },
            text = { Text(stringResource(Res.string.quest_bothway_aerialway_confirm)) }
        )
    }
}
