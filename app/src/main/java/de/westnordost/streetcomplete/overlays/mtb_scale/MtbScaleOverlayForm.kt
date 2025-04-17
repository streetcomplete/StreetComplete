package de.westnordost.streetcomplete.overlays.mtb_scale

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.mtb_scale.MtbScale
import de.westnordost.streetcomplete.osm.mtb_scale.applyTo
import de.westnordost.streetcomplete.osm.mtb_scale.asItem
import de.westnordost.streetcomplete.osm.mtb_scale.parseMtbScale
import de.westnordost.streetcomplete.overlays.AImageSelectOverlayForm

class MtbScaleOverlayForm : AImageSelectOverlayForm<MtbScale>() {

    override val items = (0..6).map { MtbScale(it).asItem() }

    override val cellLayoutId = R.layout.cell_labeled_icon_select_mtb_scale

    private var originalMtbScale: MtbScale? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originalMtbScale = parseMtbScale(element!!.tags)
        selectedItem = originalMtbScale?.asItem()
    }

    override fun hasChanges(): Boolean =
        selectedItem?.value?.value != originalMtbScale?.value

    override fun onClickOk() {
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        selectedItem!!.value!!.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }
}
