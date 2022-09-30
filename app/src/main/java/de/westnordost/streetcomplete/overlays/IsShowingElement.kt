package de.westnordost.streetcomplete.overlays

import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey

interface IsShowingElement {
    /** The element that is showing right now, if any */
    val elementKey: ElementKey?
}
