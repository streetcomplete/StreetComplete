package de.westnordost.streetcomplete.overlays.cycleway

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway
import de.westnordost.streetcomplete.osm.cycleway_separate.applyTo
import de.westnordost.streetcomplete.osm.cycleway_separate.getIcon
import de.westnordost.streetcomplete.osm.cycleway_separate.parseSeparateCycleway
import de.westnordost.streetcomplete.osm.cycleway_separate.title
import de.westnordost.streetcomplete.overlays.AItemSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.util.ktx.valueOfOrNull
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class SeparateCyclewayForm : AItemSelectOverlayForm<SeparateCycleway>() {

    override val items = SeparateCycleway.entries
    override val itemsPerRow = 1

    private val prefs: Preferences by inject()

    override val lastPickedItem: SeparateCycleway? get() =
        prefs.getLastPicked(this::class.simpleName!!)
            .map { valueOfOrNull<SeparateCycleway>(it) }
            .firstOrNull()

    @Composable override fun BoxScope.ItemContent(item: SeparateCycleway) {
        ImageWithDescription(
            painter = painterResource(item.getIcon(countryInfo.isLeftHandTraffic)),
            title = null,
            description = stringResource(item.title)
        )
    }

    private var originalCycleway: SeparateCycleway? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalCycleway = parseSeparateCycleway(element!!.tags)
        selectedItem.value = originalCycleway
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun hasChanges(): Boolean = selectedItem.value != originalCycleway

    override fun onClickOk() {
        prefs.addLastPicked(this::class.simpleName!!, selectedItem.value!!.name)
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        selectedItem.value!!.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }
}
