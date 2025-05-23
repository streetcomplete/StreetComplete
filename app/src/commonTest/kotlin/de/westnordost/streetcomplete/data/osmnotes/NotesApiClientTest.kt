package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.QueryTooBigException
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.user.UserAccessTokenSource
import de.westnordost.streetcomplete.testutils.OsmDevApi
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

// other than some other APIs we are speaking to, we do not control the OSM API, so I think it is
// more effective to test with the official test API instead of mocking some imagined server
// response
class NotesApiClientTest {

    @Test fun `create note`(): Unit = runBlocking {
        val note = client(OsmDevApi.ALLOW_EVERYTHING_TOKEN).create(LatLon(83.0, 9.0), "Created note!")
        closeNote(note.id)

        assertEquals(LatLon(83.0, 9.0), note.position)
        assertEquals(Note.Status.OPEN, note.status)
        assertEquals(1, note.comments.size)

        val comment = note.comments.first()
        assertEquals("Created note!", comment.text)
        assertEquals(NoteComment.Action.OPENED, comment.action)
        assertEquals("westnordost", comment.user?.displayName)
    }

    @Test fun `comment note`(): Unit = runBlocking {
        var note = client(OsmDevApi.ALLOW_EVERYTHING_TOKEN).create(LatLon(83.0, 9.1), "Created note for comment!")
        note = client(OsmDevApi.ALLOW_EVERYTHING_TOKEN).comment(note.id, "First comment!")
        closeNote(note.id)

        assertEquals(2, note.comments.size)
        assertEquals("Created note for comment!", note.comments[0].text)
        assertEquals(NoteComment.Action.OPENED, note.comments[0].action)
        assertEquals("westnordost", note.comments[0].user?.displayName)

        assertEquals("First comment!", note.comments[1].text)
        assertEquals(NoteComment.Action.COMMENTED, note.comments[1].action)
        assertEquals("westnordost", note.comments[1].user?.displayName)
    }

    @Test fun `comment note fails when not logged in`(): Unit = runBlocking {
        val note = client(OsmDevApi.ALLOW_EVERYTHING_TOKEN).create(LatLon(83.0, 9.1), "Created note for comment!")
        assertFailsWith<AuthorizationException> {
            client(null).comment(note.id, "test")
        }
        closeNote(note.id)
    }

    @Test fun `comment note fails when not authorized`(): Unit = runBlocking {
        val note = client(OsmDevApi.ALLOW_EVERYTHING_TOKEN).create(LatLon(83.0, 9.1), "Created note for comment!")
        assertFailsWith<AuthorizationException> {
            client(OsmDevApi.ALLOW_NOTHING_TOKEN).comment(note.id, "test")
        }
        closeNote(note.id)
    }

    @Test fun `comment note fails when already closed`(): Unit = runBlocking {
        val note = client(OsmDevApi.ALLOW_EVERYTHING_TOKEN).create(LatLon(83.0, 9.1), "Created note for comment!")
        closeNote(note.id)
        assertFailsWith<ConflictException> {
            client(OsmDevApi.ALLOW_EVERYTHING_TOKEN).comment(note.id, "test")
        }
    }

    @Test fun `get note`(): Unit = runBlocking {
        val note = client(OsmDevApi.ALLOW_EVERYTHING_TOKEN).create(LatLon(83.0, 9.2), "Created note to get it!")
        val note2 = client(null).get(note.id)
        closeNote(note.id)

        assertEquals(note, note2)
    }

    @Test fun `get no note`(): Unit = runBlocking {
        assertNull(client(null).get(0))
    }

    @Test fun `get notes`(): Unit = runBlocking {
        val note1 = client(OsmDevApi.ALLOW_EVERYTHING_TOKEN).create(LatLon(83.0, 9.3), "Note a")
        val note2 = client(OsmDevApi.ALLOW_EVERYTHING_TOKEN).create(LatLon(83.1, 9.4), "Note b")

        val notes = client(null).getAllOpen(BoundingBox(83.0, 9.3, 83.2, 9.5))

        closeNote(note1.id)
        closeNote(note2.id)

        assertTrue(notes.isNotEmpty())
    }

    @Test fun `get notes fails when bbox crosses 180th meridian`(): Unit = runBlocking {
        assertFailsWith<IllegalArgumentException> {
            client(null).getAllOpen(BoundingBox(0.0, 179.0, 0.1, -179.0))
        }
    }

    @Test fun `get notes fails when limit is too large`(): Unit = runBlocking {
        assertFailsWith<QueryTooBigException> {
            client(null).getAllOpen(BoundingBox(0.0, 0.0, 0.1, 0.1), 100000000)
        }
        assertFailsWith<QueryTooBigException> {
            client(null).getAllOpen(BoundingBox(0.0, 0.0, 90.0, 90.0))
        }
    }

    private fun client(token: String?) =
        NotesApiClient(
            httpClient = HttpClient(),
            baseUrl = OsmDevApi.URL,
            userAccessTokenSource = object : UserAccessTokenSource { override val accessToken = token.orEmpty() },
            notesApiParser = NotesApiParser()
        )

    // for cleanup
    private fun closeNote(id: Long): Unit = runBlocking {
        HttpClient().post(OsmDevApi.URL + "notes/$id/close") {
            bearerAuth(OsmDevApi.ALLOW_EVERYTHING_TOKEN)
            parameter("text", "")
        }
    }
}
