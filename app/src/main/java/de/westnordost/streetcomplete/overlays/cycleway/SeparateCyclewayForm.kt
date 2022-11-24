package de.westnordost.streetcomplete.overlays.cycleway

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway
import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway.*
import de.westnordost.streetcomplete.osm.cycleway_separate.applyTo
import de.westnordost.streetcomplete.osm.cycleway_separate.asItem
import de.westnordost.streetcomplete.overlays.AImageSelectOverlayForm
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class SeparateCyclewayForm : AImageSelectOverlayForm<SeparateCycleway>() {

    override val items: List<DisplayItem<SeparateCycleway>> =
        listOf(NONE, NON_SEGREGATED, SEGREGATED, EXCLUSIVE, WITH_SIDEWALK).map { it.asItem() }

    private var currentCycleway: SeparateCycleway? = null

    override fun hasChanges(): Boolean =
        selectedItem?.value != currentCycleway

    override fun onClickOk() {
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        selectedItem!!.value!!.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(tagChanges.create()))
    }
}
