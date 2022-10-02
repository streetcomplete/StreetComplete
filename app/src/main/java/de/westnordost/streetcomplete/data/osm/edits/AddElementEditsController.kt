package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.quest.QuestKey

interface AddElementEditsController {
    fun add(
        type: ElementEditType,
        element: Element,
        geometry: ElementGeometry,
        source: String,
        action: ElementEditAction,
        key: QuestKey? = null
    )
}
