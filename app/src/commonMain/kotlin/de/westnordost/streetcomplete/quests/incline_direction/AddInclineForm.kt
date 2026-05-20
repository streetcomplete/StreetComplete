package de.westnordost.streetcomplete.quests.incline_direction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.util.math.getOrientationOrZero
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddInclineForm(
    onAnswer: (Incline) -> Unit,
    geometry: ElementGeometry,
) {
    val geometryRotation = remember(geometry) { geometry.getOrientationOrZero() }
    ItemSelectQuestForm(
        items = Incline.entries,
        itemsPerRow = 2,
        itemContent = { item ->
            ImageWithLabel(
                painter = painterResource(item.icon),
                label = stringResource(Res.string.quest_steps_incline_up),
                imageRotation = geometryRotation - LocalMapRotation.current
            )
        },
        onClickOk = onAnswer,
    )
}
