package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.util.ktx.attribute
import de.westnordost.streetcomplete.util.ktx.endTag
import de.westnordost.streetcomplete.util.ktx.startTag
import kotlinx.datetime.Instant
import nl.adaptivity.xmlutil.XmlWriter
import nl.adaptivity.xmlutil.newWriter
import nl.adaptivity.xmlutil.xmlStreaming

class MapDataApiSerializer {
    fun serializeMapDataChanges(changes: MapDataChanges, changesetId: Long): String {
        val buffer = StringBuilder()
        xmlStreaming.newWriter(buffer).serializeMapDataChanges(changes, changesetId)
        return buffer.toString()
    }
}

private fun XmlWriter.serializeMapDataChanges(changes: MapDataChanges, changesetId: Long) {
    startTag("osmChange")
    if (changes.creations.isNotEmpty()) {
        startTag("create")
        changes.creations.forEach { serializeElement(it, changesetId) }
        endTag("create")
    }
    if (changes.modifications.isNotEmpty()) {
        startTag("modify")
        changes.modifications.forEach { serializeElement(it, changesetId) }
        endTag("modify")
    }
    if (changes.deletions.isNotEmpty()) {
        startTag("delete")
        changes.deletions.forEach { serializeElement(it, changesetId) }
        endTag("delete")
    }
    endTag("osmChange")
}

private fun XmlWriter.serializeElement(element: Element, changesetId: Long) {
    startTag(element.type.name.lowercase())
    attribute("id", element.id.toString())
    attribute("version", element.version.toString())
    attribute("changeset", changesetId.toString())
    attribute("timestamp", Instant.fromEpochMilliseconds(element.timestampEdited).toString())
    when (element) {
        is Node -> {
            attribute("lat", element.position.latitude.toString())
            attribute("lon", element.position.longitude.toString())
        }
        is Way -> {
            for (node in element.nodeIds) {
                startTag("nd")
                attribute("ref", node.toString())
                endTag("nd")
            }
        }
        is Relation -> {
            for (member in element.members) {
                startTag("member")
                attribute("type", member.type.name.lowercase())
                attribute("ref", member.ref.toString())
                attribute("role", member.role)
                endTag("member")
            }
        }
    }
    for ((k, v) in element.tags) {
        startTag("tag")
        attribute("k", k)
        attribute("v", v)
        endTag("tag")
    }
    endTag(element.type.name.lowercase())
}
