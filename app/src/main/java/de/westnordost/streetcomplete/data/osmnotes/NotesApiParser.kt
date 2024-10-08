package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.user.User
import de.westnordost.streetcomplete.util.ktx.attribute
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.serialization.SerializationException
import nl.adaptivity.xmlutil.EventType.*
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.xmlStreaming

class NotesApiParser {
    fun parseNotes(osmXml: String): List<Note> =
        xmlStreaming.newReader(osmXml).parseNotes()
}

private fun XmlReader.parseNotes(): List<Note> = try {
    val result = ArrayList<Note>()

    var note: ApiNote? = null
    var comment: ApiNoteComment? = null
    var string = ""

    forEach { when (it) {
        START_ELEMENT -> {
            when (localName) {
                "note" -> note = ApiNote(LatLon(attribute("lat").toDouble(), attribute("lon").toDouble()))
                "comment" -> comment = ApiNoteComment()
            }
            string = ""
        }
        // regarding IGNORABLE_WHITESPACE: https://github.com/pdvrieze/xmlutil/issues/241
        TEXT, ENTITY_REF, CDSECT, IGNORABLE_WHITESPACE -> {
            string += text
        }
        END_ELEMENT -> when (localName) {
            // in note
            "id" -> note?.id = string.toLong()
            "date_created" -> note?.timestampCreated = parseTimestamp(string)
            "date_closed" -> note?.timestampClosed = parseTimestamp(string)
            "status" -> note?.status = Note.Status.valueOf(string.uppercase())

            // in comment
            "date" -> comment?.date = parseTimestamp(string)
            "action" -> comment?.action = NoteComment.Action.valueOf(string.uppercase())
            "text" -> comment?.text = string
            "uid" -> comment?.uid = string.toLong()
            "user" -> comment?.user = string

            // note
            "note" -> {
                val n = note!!
                result.add(Note(n.position, n.id!!, n.timestampCreated!!, n.timestampClosed, n.status!!, n.comments))
            }
            // comment
            "comment" -> {
                val c = comment!!
                val cUser = if (c.user != null && c.uid != null) User(c.uid!!, c.user!!) else null
                note?.comments?.add(NoteComment(c.date!!, c.action!!, c.text, cUser))
            }
        }
        else -> {}
    } }
    result
} catch (e: Exception) { throw SerializationException(e) }

private data class ApiNote(
    val position: LatLon,
    var id: Long? = null,
    var timestampCreated: Long? = null,
    var timestampClosed: Long? = null,
    var status: Note.Status? = null,
    val comments: MutableList<NoteComment> = ArrayList(),
)

private data class ApiNoteComment(
    var date: Long? = null,
    var action: NoteComment.Action? = null,
    var text: String? = null,
    var uid: Long? = null,
    var user: String? = null,
)

private val dateFormat = DateTimeComponents.Format {
    date(LocalDate.Formats.ISO)
    char(' ')
    time(LocalTime.Formats.ISO)
    char(' ')
    timeZoneId()
}

private fun parseTimestamp(date: String): Long =
    dateFormat.parse(date).toInstantUsingOffset().toEpochMilliseconds()
