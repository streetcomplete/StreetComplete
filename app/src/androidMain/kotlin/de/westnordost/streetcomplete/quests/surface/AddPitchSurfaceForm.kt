package de.westnordost.streetcomplete.quests.surface

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddPitchSurfaceForm : AImageListQuestForm<Surface, Surface>() {
    override val items get() = Surface.selectableValuesForPitches

    override val itemsPerRow = 3

    @Composable override fun BoxScope.ItemContent(item: Surface) {
        ImageWithLabel(item.icon?.let { painterResource(it) }, stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<Surface>) {
        applyAnswer(selectedItems.single())
    }
}
