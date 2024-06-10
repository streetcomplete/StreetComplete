package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerialName

class ChangesetApiSerializer {
    private val xml = XML { defaultPolicy { ignoreUnknownChildren() }}

    fun serialize(changesetTags: Map<String, String>): String {
        return xml.encodeToString(changesetTags.toApiOsm())
    }
}

private fun Map<String, String>.toApiOsm() =
    ApiOsm(changeset = ApiChangeset(tags = toApiTags()))

private fun Map<String, String>.toApiTags(): List<ApiTag> =
    map { (k, v) -> ApiTag(k, v) }

@Serializable
@XmlSerialName("osm")
private data class ApiOsm(val changeset: ApiChangeset)

@Serializable
@XmlSerialName("changeset")
private data class ApiChangeset(val tags: List<ApiTag>)

@Serializable
@XmlSerialName("tag")
private data class ApiTag(val k: String, val v: String)
