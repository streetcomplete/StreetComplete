package de.westnordost.streetcomplete.data.osm.changes

import de.westnordost.osmapi.map.data.Element

interface HasElementTagChanges {
    val changes: StringMapChanges
    fun isApplicableTo(element: Element): Boolean?
}
