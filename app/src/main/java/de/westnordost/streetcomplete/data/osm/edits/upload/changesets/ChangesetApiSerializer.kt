package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import de.westnordost.streetcomplete.util.ktx.attribute
import de.westnordost.streetcomplete.util.ktx.endTag
import de.westnordost.streetcomplete.util.ktx.startTag
import nl.adaptivity.xmlutil.XmlWriter
import nl.adaptivity.xmlutil.newWriter
import nl.adaptivity.xmlutil.xmlStreaming

class ChangesetApiSerializer {
    fun serialize(changesetTags: Map<String, String>): String {
        val buffer = StringBuilder()
        xmlStreaming.newWriter(buffer).serializeChangeset(changesetTags)
        return buffer.toString()
    }
}

private fun XmlWriter.serializeChangeset(changesetTags: Map<String, String>) {
    startTag("osm")
    startTag("changeset")
    for ((k, v) in changesetTags) {
        startTag("tag")
        attribute("k", k)
        attribute("v", v)
        endTag("tag")
    }
    endTag("changeset")
    endTag("osm")
}
