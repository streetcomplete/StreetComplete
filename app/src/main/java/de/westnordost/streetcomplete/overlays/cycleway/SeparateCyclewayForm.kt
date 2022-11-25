package de.westnordost.streetcomplete.overlays.cycleway

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway
import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway.*
import de.westnordost.streetcomplete.osm.cycleway_separate.applyTo
import de.westnordost.streetcomplete.osm.cycleway_separate.asItem
import de.westnordost.streetcomplete.osm.cycleway_separate.createSeparateCycleway
import de.westnordost.streetcomplete.overlays.AImageSelectOverlayForm
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class SeparateCyclewayForm : AImageSelectOverlayForm<SeparateCycleway>() {

    override val items: List<DisplayItem<SeparateCycleway>> get() =
        listOf(NON_DESIGNATED, NON_SEGREGATED, SEGREGATED, EXCLUSIVE, EXCLUSIVE_WITH_SIDEWALK).map {
            it.asItem(countryInfo.isLeftHandTraffic)
        }

    override val itemsPerRow = 1
    override val cellLayoutId = R.layout.cell_labeled_icon_select_right

    private var currentCycleway: SeparateCycleway? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cycleway = createSeparateCycleway(element!!.tags)

        /* Not displaying bicycle=yes and bicycle=no on footways and treating it the same because
           whether riding a bike on a footway is allowed by default (without requiring signs) or
           only under certain conditions (e.g. certain minimum width of sidewalk) is very much
           dependent on the country or state one is in.

           Hence, it is not verifiable well for the on-site surveyor: If there is no sign that
           specifically allows or forbids cycling on a footway, the user is left with his loose
           (mis)understanding of the local legislation to decide. After all, bicycle=yes/no
           is (usually) nothing physical, but merely describes what is legal. It is in that sense
           then not information surveyable  on-the-ground, unless specifically signed.
           bicycle=yes/no does not make a statement about from where this info is derived.

           So, from an on-site surveyor point of view, it is always better to record what is signed,
           instead of what follows from that signage.
           Signage, however, is out of scope of this overlay. It would be better fitted as a quest (in
           countries and states where frequent signage is to be expected) */
        currentCycleway = if (cycleway == NONE || cycleway == ALLOWED) NON_DESIGNATED else cycleway
        selectedItem = currentCycleway?.asItem(countryInfo.isLeftHandTraffic)
    }

    override fun hasChanges(): Boolean =
        selectedItem?.value != currentCycleway

    override fun onClickOk() {
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        selectedItem!!.value!!.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(tagChanges.create()))
    }
}
