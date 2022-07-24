package de.westnordost.streetcomplete.overlays.tracktype

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.Tracktype
import de.westnordost.streetcomplete.osm.applyTo
import de.westnordost.streetcomplete.osm.createTracktypeStatus
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.overlays.AImageSelectOverlayForm
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class TracktypeOverlayForm : AImageSelectOverlayForm<Tracktype>() {

    override val items: List<DisplayItem<Tracktype>> = Tracktype.items()

    private var currentStatus: Tracktype? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val status = createTracktypeStatus(element.tags)
        currentStatus = status
        if (status != null) {
            selectedItem = status.asItem()
        }
    }

    override fun hasChanges(): Boolean =
        selectedItem?.value != currentStatus

    override fun onClickOk() {
        applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element.tags).also {
            selectedItem!!.value!!.applyTo(it)
            //or maybe just this?
            //it.updateWithCheckDate("tracktype", selectedItem!!.value!!.osmValue)
        }.create()))
    }
}
