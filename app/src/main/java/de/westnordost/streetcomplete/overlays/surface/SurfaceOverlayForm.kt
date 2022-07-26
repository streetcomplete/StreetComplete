package de.westnordost.streetcomplete.overlays.surface

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.Surface
import de.westnordost.streetcomplete.osm.createSurfaceStatus
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.overlays.AImageSelectOverlayForm
import de.westnordost.streetcomplete.quests.surface.GENERIC_ROAD_SURFACES
import de.westnordost.streetcomplete.quests.surface.asItem
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class SurfaceOverlayForm : AImageSelectOverlayForm<Surface>() {

    override val items: List<DisplayItem<Surface>> = Surface.values().filter { it !in GENERIC_ROAD_SURFACES }.map { it.asItem() }
    override val itemsPerRow = 4 // 2 or 3 for normal user

    private var currentStatus: Surface? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val status = createSurfaceStatus(element.tags)
        currentStatus = status
        if (status != null) {
            selectedItem = status.asItem()
        }
    }

    override fun hasChanges(): Boolean =
        selectedItem?.value != currentStatus

    override fun onClickOk() {
        applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element.tags).also {
            it.updateWithCheckDate("surface", selectedItem!!.value!!.osmValue)
        }.create()))
    }
}
