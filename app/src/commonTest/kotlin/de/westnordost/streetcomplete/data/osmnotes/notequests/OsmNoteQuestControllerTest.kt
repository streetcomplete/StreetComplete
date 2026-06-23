package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.user.User
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.UserLoginSource
import dev.mokkery.matcher.any
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.comment
import dev.mokkery.mock
import de.westnordost.streetcomplete.testutils.note
import dev.mokkery.answering.returns
import dev.mokkery.every
import de.westnordost.streetcomplete.testutils.osmNoteQuest
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import dev.mokkery.answering.calls
import dev.mokkery.matcher.matches
import dev.mokkery.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OsmNoteQuestControllerTest {

    private lateinit var noteSource: NotesWithEditsSource
    private lateinit var userDataSource: UserDataSource
    private lateinit var userLoginSource: UserLoginSource
    private lateinit var prefs: Preferences

    private lateinit var ctrl: OsmNoteQuestController
    private lateinit var listener: OsmNoteQuestSource.Listener

    private lateinit var noteUpdatesListener: NotesWithEditsSource.Listener
    private lateinit var userLoginListener: UserLoginSource.Listener

    @BeforeTest fun setUp() {
        noteSource = mock() {
            every { addListener(any()) } calls { (listener: NotesWithEditsSource.Listener) ->
                noteUpdatesListener = listener
            }
        }
        userDataSource = mock()
        userLoginSource = mock() {
            every { addListener(any()) } calls { (listener: UserLoginSource.Listener) ->
                userLoginListener = listener
            }
        }
        prefs = mock()

        listener = mock()

        ctrl = OsmNoteQuestController(noteSource, userDataSource, userLoginSource, prefs)
        ctrl.addListener(listener)
    }

    @Test fun `get missing returns null`() {
        every { noteSource.get(1) } returns null
        assertNull(ctrl.get(1))
    }

    @Test fun `get note quest with comment from user returns null`() {
        every { noteSource.get(1) } returns note(comments = listOf(
            comment(text = "test?", user = User(id = 100, "Blaubär")),
            comment(text = "test", user = User(id = 1, "Blubbi"))
        ))
        every { userDataSource.userId } returns 1

        assertNull(ctrl.get(1))
    }

    @Test fun `get note quest with comment from user that contains a survey required marker returns non-null`() {
        every { noteSource.get(1) } returns
            note(comments = listOf(
                comment(text = "test?", user = User(id = 100, "Blaubär")),
                comment(text = "ok but #surveyme", user = User(id = 1, "Blubbi")),
            ))
        every { userDataSource.userId } returns 1

        assertNotNull(ctrl.get(1))
    }

    @Test fun `get note quest created in app without comments and without survey required marker returns null`() {
        every { noteSource.get(1) } returns
            note(comments = listOf(
                comment(
                    text = "this is a non-question test note\n\nCreated via StreetComplete 60.1",
                    user = User(id = 100, "Blaubär")
                ),
            ))
        every { userDataSource.userId } returns 1

        assertNull(ctrl.get(1))
    }

    @Test fun `get note quest created in app without comments and with survey required marker returns non-null`() {
        every { noteSource.get(1) } returns
            note(comments = listOf(
                comment(
                    text = "this is a non-question test note #surveyme\n\nCreated via StreetComplete 60.1",
                    user = User(id = 100, "Blaubär")
                ),
            ))
        every { userDataSource.userId } returns 1

        assertNotNull(ctrl.get(1))
    }

    @Test fun `get note quest created in app without comments and with survey required marker (ignore case) returns non-null`() {
        every { noteSource.get(1) } returns
            note(comments = listOf(
                comment(
                    text = "this is a non-question test note #SurVEyMe\n\nCreated via StreetComplete 60.1",
                    user = User(id = 100, "Blaubär")
                ),
            ))
        every { userDataSource.userId } returns 1

        assertNotNull(ctrl.get(1))
    }

    @Test fun `get quest not phrased as question returns null`() {
        every { noteSource.get(1) } returns note(comments = listOf(comment(text = "test")))
        every { prefs.showAllNotes } returns false

        assertNull(ctrl.get(1))
    }

    @Test fun `get quest phrased as question returns non-null`() {
        every { noteSource.get(1) } returns
            note(
                id = 1,
                position = p(1.0, 1.0),
                comments = listOf(comment(text = "test?"))
            )
        every { prefs.showAllNotes } returns false

        assertEquals(osmNoteQuest(1, p(1.0, 1.0)), ctrl.get(1))
    }

    @Test fun `get quest phrased as question in other scripts returns non-null`() {
        every { noteSource.get(1) } returns note(1, comments = listOf(comment(text = "Greek question mark: ;")))
        every { noteSource.get(2) } returns note(2, comments = listOf(comment(text = "semicolon: ;")))
        every { noteSource.get(3) } returns note(3, comments = listOf(comment(text = "mirrored question mark: ؟")))
        every { noteSource.get(4) } returns note(4, comments = listOf(comment(text = "Armenian question mark: ՞")))
        every { noteSource.get(5) } returns note(5, comments = listOf(comment(text = "Ethiopian question mark: ፧")))
        every { noteSource.get(6) } returns note(6, comments = listOf(comment(text = "Vai question mark: ꘏")))
        every { noteSource.get(7) } returns note(7, comments = listOf(comment(text = "full width question mark: ？")))
        every { prefs.showAllNotes } returns false

        assertEquals(1, ctrl.get(1)?.id)
        assertEquals(2, ctrl.get(2)?.id)
        assertEquals(3, ctrl.get(3)?.id)
        assertEquals(4, ctrl.get(4)?.id)
        assertEquals(5, ctrl.get(5)?.id)
        assertEquals(6, ctrl.get(6)?.id)
        assertEquals(7, ctrl.get(7)?.id)
    }

    @Test fun `get quest with comment containing survey required marker returns non-null`() {
        every { noteSource.get(1) } returns
            note(
                id = 1,
                position = p(1.0, 1.0),
                comments = listOf(comment(text = "test #surveyme"))
            )
        every { prefs.showAllNotes } returns false

        assertEquals(osmNoteQuest(1, p(1.0, 1.0)), ctrl.get(1))
    }

    @Test fun `get quest not phrased as question returns non-null by preference`() {
        every { noteSource.get(1) } returns
            note(
                id = 1,
                position = p(1.0, 1.0),
                comments = listOf(comment(text = "test"))
            )
        every { prefs.showAllNotes } returns true

        assertEquals(osmNoteQuest(1, p(1.0, 1.0)), ctrl.get(1))
    }

    // not doing all the tests for getAll again because it uses the same functions

    @Test fun getAll() {
        val bbox = bbox()
        val notes = listOf(note(1), note(2), note(3))

        every { noteSource.getAll(bbox) } returns notes
        every { prefs.showAllNotes } returns true

        val expectedQuests = notes.map { osmNoteQuest(it.id, it.position) }

        assertEquals(
            expectedQuests,
            ctrl.getAllInBBox(bbox)
        )
    }

    @Test fun `calls onInvalidated when logged in`() {
        userLoginListener.onLoggedIn()
        verify { listener.onInvalidated() }
    }

    @Test fun `calls onInvalidated when cleared notes`() {
        noteUpdatesListener.onCleared()
        verify { listener.onInvalidated() }
    }

    @Test fun `calls onUpdated when notes changed`() {
        every { prefs.showAllNotes } returns true

        noteUpdatesListener.onUpdated(
            added = listOf(note(1)),
            updated = listOf(note(2)),
            deleted = listOf(3)
        )

        verify {
            listener.onUpdated(
                added = matches { it.containsExactlyInAnyOrder(listOf(osmNoteQuest(1, p()), osmNoteQuest(2, p()))) },
                deleted = matches { it.containsExactlyInAnyOrder(listOf(3)) }
            )
        }
    }
}
