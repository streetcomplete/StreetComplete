package de.westnordost.streetcomplete.quests.surface

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddPathPartSurfaceForm(
    on: (QuestAction<Surface>) -> Unit,
) {
    ItemSelectQuestForm(
        on = on,
        items = Surface.selectableValuesForWays,
        itemContent = { item ->
            ImageWithLabel(item.icon?.let { painterResource(it) }, stringResource(item.title))
        },
        favoriteKey = "AddPathPartSurfaceForm",
    )
}
