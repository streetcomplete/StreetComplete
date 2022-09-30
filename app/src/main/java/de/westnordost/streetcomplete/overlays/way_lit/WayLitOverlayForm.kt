package de.westnordost.streetcomplete.overlays.way_lit

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.lit.LitStatus
import de.westnordost.streetcomplete.osm.lit.LitStatus.AUTOMATIC
import de.westnordost.streetcomplete.osm.lit.LitStatus.NIGHT_AND_DAY
import de.westnordost.streetcomplete.osm.lit.LitStatus.NO
import de.westnordost.streetcomplete.osm.lit.LitStatus.UNSUPPORTED
import de.westnordost.streetcomplete.osm.lit.LitStatus.YES
import de.westnordost.streetcomplete.osm.lit.applyTo
import de.westnordost.streetcomplete.osm.lit.asItem
import de.westnordost.streetcomplete.osm.lit.createLitStatus
import de.westnordost.streetcomplete.overlays.AImageSelectOverlayForm
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class WayLitOverlayForm : AImageSelectOverlayForm<LitStatus>() {

    override val items: List<DisplayItem<LitStatus>> =
        listOf(YES, NO, AUTOMATIC, NIGHT_AND_DAY).map { it.asItem() }

    private var currentLitStatus: LitStatus? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val litStatus = createLitStatus(element!!.tags)
        currentLitStatus = litStatus
        if (litStatus != null && litStatus != UNSUPPORTED) {
            selectedItem = litStatus.asItem()
        }
    }

    override fun hasChanges(): Boolean =
        selectedItem?.value != currentLitStatus

    override fun onClickOk() {
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        selectedItem!!.value!!.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(tagChanges.create()))
    }
}
