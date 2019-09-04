package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges

interface HasElementTagChanges {
    val changes: StringMapChanges?
    fun isApplicableTo(element: Element): Boolean?
}
