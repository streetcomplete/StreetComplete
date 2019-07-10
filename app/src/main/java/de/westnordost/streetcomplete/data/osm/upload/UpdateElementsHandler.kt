package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.osmapi.common.Handler
import de.westnordost.osmapi.map.changes.DiffElement
import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.data.osm.ElementKey
import de.westnordost.streetcomplete.ktx.copy
import java.util.ArrayList

class UpdateElementsHandler(val elements: Collection<Element>) : Handler<DiffElement> {
    val updatedElements: MutableList<Element> = ArrayList()
    val deletedElementsKeys: MutableList<ElementKey> = ArrayList()

    override fun handle(d: DiffElement) {
        val element = elements.find { it.type == d.type && it.id == d.clientId } ?: return
        if (d.serverVersion == null || d.serverId == null) {
            deletedElementsKeys.add(ElementKey(element.type, element.id))
        }
        else if (element.version != d.serverVersion || element.id != d.serverId) {
            updatedElements.add(element.copy(d.serverId, d.serverVersion))
        }
    }
}

