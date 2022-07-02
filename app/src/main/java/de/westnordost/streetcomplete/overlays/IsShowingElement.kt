package de.westnordost.streetcomplete.overlays

import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey

interface IsShowingElement {
    val elementKey: ElementKey
}
