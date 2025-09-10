package de.westnordost.streetcomplete.overlays.mtb_scale

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.mtb_scale.MtbScale
import de.westnordost.streetcomplete.osm.mtb_scale.applyTo
import de.westnordost.streetcomplete.osm.mtb_scale.parseMtbScale
import de.westnordost.streetcomplete.overlays.AImageSelectOverlayForm
import de.westnordost.streetcomplete.util.ktx.dpToPx

class MtbScaleOverlayForm : AImageSelectOverlayForm<MtbScale>() {

    override val items = (0..6).map { MtbScale(it) }

    override val itemsPerRow = 1

    private var originalMtbScale: MtbScale? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val select = view.findViewById<View>(R.id.selectButton)
        val params = select.layoutParams as ConstraintLayout.LayoutParams
        params.matchConstraintMaxWidth = resources.dpToPx(280).toInt()
        select.requestLayout()

        originalMtbScale = parseMtbScale(element!!.tags)
        selectedItem = originalMtbScale
    }

    override fun hasChanges(): Boolean = selectedItem != originalMtbScale

    override fun onClickOk() {
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        selectedItem!!.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }
}
