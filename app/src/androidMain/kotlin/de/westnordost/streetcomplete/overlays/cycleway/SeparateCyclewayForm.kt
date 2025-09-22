package de.westnordost.streetcomplete.overlays.cycleway

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway
import de.westnordost.streetcomplete.osm.cycleway_separate.applyTo
import de.westnordost.streetcomplete.osm.cycleway_separate.getIcon
import de.westnordost.streetcomplete.osm.cycleway_separate.parseSeparateCycleway
import de.westnordost.streetcomplete.osm.cycleway_separate.title
import de.westnordost.streetcomplete.overlays.AItemSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class SeparateCyclewayForm : AItemSelectOverlayForm<SeparateCycleway>() {

    override val items = SeparateCycleway.entries
    override val itemsPerRow = 1

    @Composable override fun ItemContent(item: SeparateCycleway) {
        ImageWithDescription(
            painter = painterResource(item.getIcon(countryInfo.isLeftHandTraffic)),
            title = null,
            description = stringResource(item.title)
        )
    }

    @Composable override fun LastPickedItemContent(item: SeparateCycleway) {
        val icon = item.getIcon(countryInfo.isLeftHandTraffic)
        Image(painterResource(icon), stringResource(item.title), Modifier.height(36.dp))
    }

    private var originalCycleway: SeparateCycleway? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalCycleway = parseSeparateCycleway(element!!.tags)
        selectedItem.value = originalCycleway
    }

    override fun hasChanges(): Boolean = selectedItem.value != originalCycleway

    override fun onClickOk(selectedItem: SeparateCycleway) {
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        selectedItem.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }
}
