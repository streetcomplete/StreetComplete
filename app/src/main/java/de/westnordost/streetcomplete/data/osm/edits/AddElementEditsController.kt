package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element

interface AddElementEditsController {
    fun add(
        type: ElementEditType,
        geometry: ElementGeometry,
        source: String,
        action: ElementEditAction
    )
}
