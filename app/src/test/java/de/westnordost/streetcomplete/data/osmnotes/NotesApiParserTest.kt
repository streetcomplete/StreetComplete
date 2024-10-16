package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.user.User
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class NotesApiParserTest {

    @Test fun `parse one minimum note`() {
        val xml = """
        <osm>
          <note lon="0.0689357" lat="51.5085707">
            <id>1</id>
            <date_created>2024-06-06 12:47:50 UTC</date_created>
            <status>open</status>
          </note>
        </osm>
        """.trimIndent()

        val note = Note(
            position = LatLon(51.5085707, 0.0689357),
            id = 1,
            timestampCreated = Instant.parse("2024-06-06T12:47:50Z").toEpochMilliseconds(),
            timestampClosed = null,
            status = Note.Status.OPEN,
            comments = listOf()
        )

        assertEquals(listOf(note), NotesApiParser().parseNotes(xml))
    }

    @Test fun `parse one full note`() {
        val xml = """
        <osm version="0.6" generator="OpenStreetMap server" copyright="OpenStreetMap and contributors" attribution="http://www.openstreetmap.org/copyright" license="http://opendatacommons.org/licenses/odbl/1-0/">
          <note lon="0.0689357" lat="51.5085707">
            <id>1</id>
            <url>https://api.openstreetmap.org/api/0.6/notes/1</url>
            <comment_url>https://api.openstreetmap.org/api/0.6/notes/1/comment</comment_url>
            <close_url>https://api.openstreetmap.org/api/0.6/notes/1/close</close_url>
            <date_created>2024-06-06 12:47:50 UTC</date_created>
            <status>closed</status>
            <date_closed>2024-06-06 12:47:51 UTC</date_closed>
            <comments>
              <comment>
                <date>2024-06-06 12:47:50 UTC</date>
                <uid>1234</uid>
                <user>dude</user>
                <user_url>https://api.openstreetmap.org/user/dude</user_url>
                <action>opened</action>
                <text>I opened it!</text>
                <html><p>Some</p><p>text</p></html>
              </comment>
              <comment>
                <date>2024-06-06 12:47:51 UTC</date>
                <action>closed</action>
              </comment>
            </comments>
          </note>
        </osm>
        """.trimIndent()

        val note = Note(
            position = LatLon(51.5085707, 0.0689357),
            id = 1,
            timestampCreated = Instant.parse("2024-06-06T12:47:50Z").toEpochMilliseconds(),
            timestampClosed = Instant.parse("2024-06-06T12:47:51Z").toEpochMilliseconds(),
            status = Note.Status.CLOSED,
            comments = listOf(
                NoteComment(
                    timestamp = Instant.parse("2024-06-06T12:47:50Z").toEpochMilliseconds(),
                    action = NoteComment.Action.OPENED,
                    text = "I opened it!",
                    user = User(1234, "dude")
                ),
                NoteComment(
                    timestamp = Instant.parse("2024-06-06T12:47:51Z").toEpochMilliseconds(),
                    action = NoteComment.Action.CLOSED,
                    text = null,
                    user = null
                ),
            )
        )

        assertEquals(listOf(note), NotesApiParser().parseNotes(xml))
    }

    @Test fun `parse several notes`() {
        val xml = """
        <osm>
          <note lon="0.0689357" lat="51.5085707">
            <id>1</id>
            <date_created>2024-06-06 12:47:50 UTC</date_created>
            <status>open</status>
          </note>
          <note lon="0.0689359" lat="51.5085709">
            <id>2</id>
            <date_created>2024-06-06 12:47:51 UTC</date_created>
            <status>hidden</status>
          </note>
        </osm>
        """.trimIndent()

        val notes = listOf(
            Note(
                position = LatLon(51.5085707, 0.0689357),
                id = 1,
                timestampCreated = Instant.parse("2024-06-06T12:47:50Z").toEpochMilliseconds(),
                timestampClosed = null,
                status = Note.Status.OPEN,
                comments = listOf()
            ),
            Note(
                position = LatLon(51.5085709, 0.0689359),
                id = 2,
                timestampCreated = Instant.parse("2024-06-06T12:47:51Z").toEpochMilliseconds(),
                timestampClosed = null,
                status = Note.Status.HIDDEN,
                comments = listOf()
            ),
        )

        assertEquals(notes, NotesApiParser().parseNotes(xml))
    }

    @Test fun `parse note with XML entity refs`() {
        val xml = """
        <osm>
          <note lon="0.0689357" lat="51.5085707">
            <id>1</id>
            <date_created>2024-06-06 12:47:50 UTC</date_created>
            <status>open</status>
            <comments>
              <comment>
                <date>2024-06-06 12:47:50 UTC</date>
                <uid>1234</uid>
                <user>dude &amp; &lt;dudette&gt;</user>
                <user_url>https://api.openstreetmap.org/user/dude</user_url>
                <action>opened</action>
                <text>I opened it &amp; &quot;nothing&quot; broke!</text>
                <html><p>Some</p><p>text</p></html>
              </comment>
            </comments>
          </note>
        </osm>
        """.trimIndent()

        val comment = NotesApiParser().parseNotes(xml)[0].comments[0]

        assertEquals("dude & <dudette>", comment.user?.displayName)
        assertEquals("I opened it & \"nothing\" broke!", comment.text)
    }
}
