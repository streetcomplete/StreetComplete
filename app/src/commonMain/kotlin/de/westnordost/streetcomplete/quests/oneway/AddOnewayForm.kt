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
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.util.ClipCirclePainter
import de.westnordost.streetcomplete.util.math.getOrientationOrZero
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import de.westnordost.streetcomplete.resources.*

@Composable
fun AddOnewayForm(
    on: (QuestAction<OnewayAnswer>) -> Unit,
    element: Element,
    geometry: ElementGeometry
) {
    var pendingAnswer by remember { mutableStateOf<QuestAction<OnewayAnswer>?>(null) }
    val geometryRotation = remember(geometry) { geometry.getOrientationOrZero() }
    ItemSelectQuestForm(
        on = {
            // Some types of aerialways only very rarely allow you to use them in both directions
            // https://github.com/streetcomplete/StreetComplete/issues/6970
            val rareBothwayAerialways = arrayOf("t-bar", "j-bar", "platter", "drag_lift")
            if (it is Answer<OnewayAnswer> && it.value == OnewayAnswer.NO_ONEWAY && element.tags["aerialway"] in rareBothwayAerialways) {
                pendingAnswer = it
            } else {
                on(it)
            }
        },
        items = OnewayAnswer.entries,
        itemContent = { item ->
            val painter = painterResource(item.icon)
            ImageWithLabel(
                painter = remember(painter) { ClipCirclePainter(painter) },
                label = stringResource(item.title),
                imageRotation = geometryRotation - LocalMapRotation.current
            )
        },
    )

    pendingAnswer?.let { answer ->
        AreYouSureDialog(
            onDismissRequest = { pendingAnswer = null },
            onConfirmed = { on(answer) },
            text = { Text(stringResource(Res.string.quest_bothway_aerialway_confirm)) }
        )
    }
}
