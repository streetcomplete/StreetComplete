package de.westnordost.streetcomplete.data.overlays

import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm

interface AndroidOverlay {
    /** returns the fragment in which the user can view/add the data or null if no form should be
     * displayed for the given [element]. [element] is null for when a new element should be created
     */
    fun createForm(element: Element?): AbstractOverlayForm?
}
