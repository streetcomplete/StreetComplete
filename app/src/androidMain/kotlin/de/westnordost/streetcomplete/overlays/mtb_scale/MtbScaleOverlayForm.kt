package de.westnordost.streetcomplete.overlays.mtb_scale

import android.os.Bundle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.mtb_scale.MtbScale
import de.westnordost.streetcomplete.osm.mtb_scale.applyTo
import de.westnordost.streetcomplete.osm.mtb_scale.description
import de.westnordost.streetcomplete.osm.mtb_scale.icon
import de.westnordost.streetcomplete.osm.mtb_scale.parseMtbScale
import de.westnordost.streetcomplete.osm.mtb_scale.title
import de.westnordost.streetcomplete.overlays.AItemSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.util.ktx.valueOfOrNull
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject
import kotlin.getValue

class MtbScaleOverlayForm : AItemSelectOverlayForm<MtbScale.Value>() {

    override val items = MtbScale.Value.entries
    override val itemsPerRow = 1

    private val prefs: Preferences by inject()

    private var originalMtbScale: MtbScale.Value? = null

    override val lastPickedItem: MtbScale.Value? get() =
        prefs.getLastPicked(this::class.simpleName!!)
            .map { valueOfOrNull<MtbScale.Value>(it) }
            .firstOrNull()


    @Composable override fun ItemContent(item: MtbScale.Value) {
        ImageWithDescription(
            painter = painterResource(item.icon),
            title = stringResource(item.title),
            description = stringResource(item.description)
        )
    }

    @Composable override fun LastPickedItemContent(item: MtbScale.Value) {
        Text(stringResource(item.title))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        originalMtbScale = parseMtbScale(element!!.tags)?.value
        selectedItem.value = originalMtbScale
    }

    override fun hasChanges(): Boolean = selectedItem.value != originalMtbScale

    override fun onClickOk() {
        prefs.addLastPicked(this::class.simpleName!!, selectedItem.value!!.name)
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        MtbScale(selectedItem.value!!).applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }
}
