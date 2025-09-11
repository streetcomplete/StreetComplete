package de.westnordost.streetcomplete.overlays.mtb_scale

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.constraintlayout.widget.ConstraintLayout
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.mtb_scale.MtbScale
import de.westnordost.streetcomplete.osm.mtb_scale.applyTo
import de.westnordost.streetcomplete.osm.mtb_scale.description
import de.westnordost.streetcomplete.osm.mtb_scale.icon
import de.westnordost.streetcomplete.osm.mtb_scale.parseMtbScale
import de.westnordost.streetcomplete.osm.mtb_scale.title
import de.westnordost.streetcomplete.overlays.AImageSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithDescription
import de.westnordost.streetcomplete.util.ktx.dpToPx
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class MtbScaleOverlayForm : AImageSelectOverlayForm<MtbScale>() {

    override val items = MtbScale.Value.entries.map { MtbScale(it) }
    override val itemsPerRow = 1

    private var originalMtbScale: MtbScale? = null

    @Composable override fun BoxScope.ItemContent(item: MtbScale) {
        ImageWithDescription(
            painter = painterResource(item.icon),
            title = stringResource(item.title),
            description = stringResource(item.description)
        )
    }

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
