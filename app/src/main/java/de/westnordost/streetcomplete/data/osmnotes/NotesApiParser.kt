@file:UseSerializers(NoteDateSerializer::class)

package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.user.User
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

class NotesApiParser {
    private val xmlParser = XML { defaultPolicy { ignoreUnknownChildren() }}

    fun parse(xml: String): List<Note> {
        val osm = xmlParser.decodeFromString<ApiOsm>(xml)
        return osm.notes.map { it.toNote() }
    }
}

private fun ApiNote.toNote() = Note(
    position = LatLon(lat, lon),
    id = id,
    timestampCreated = dateCreated.toEpochMilliseconds(),
    timestampClosed = dateClosed?.toEpochMilliseconds(),
    status = Note.Status.valueOf(status.uppercase()),
    comments = comments.map { it.toNoteComment() }
)

private fun ApiNoteComment.toNoteComment() = NoteComment(
    timestamp = date.toEpochMilliseconds(),
    action = NoteComment.Action.valueOf(action.uppercase()),
    text = text,
    user = if (uid != null && user != null) User(id = uid, displayName = user) else null
)

@Serializable
private data class ApiOsm(
    @XmlSerialName("note") val notes: List<ApiNote>
)

@Serializable
private data class ApiNote(
    val lon: Double,
    val lat: Double,
    @XmlElement val id: Long,
    @XmlElement @XmlSerialName("date_created") val dateCreated: Instant,
    @XmlElement @XmlSerialName("date_closed") val dateClosed: Instant?,
    @XmlElement val status: String,
    @XmlChildrenName("comment") val comments: List<ApiNoteComment>,
)

@Serializable
private data class ApiNoteComment(
    @XmlElement val date: Instant,
    @XmlElement val action: String,
    @XmlElement val text: String?,
    @XmlElement val uid: Long?,
    @XmlElement val user: String?,
)

private object NoteDateSerializer : KSerializer<Instant> {
    private val dateFormat = DateTimeComponents.Format {
        date(LocalDate.Formats.ISO)
        char(' ')
        time(LocalTime.Formats.ISO)
        char(' ')
        timeZoneId()
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return dateFormat.parse(decoder.decodeString()).toInstantUsingOffset()
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.format(dateFormat))
    }
}
