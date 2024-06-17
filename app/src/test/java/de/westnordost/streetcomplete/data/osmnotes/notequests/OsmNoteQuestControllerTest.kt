package de.westnordost.streetcomplete.data.osmnotes.notequests

import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.user.User
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.UserLoginStatusSource
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.comment
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.eq
import io.mockative.every
import io.mockative.matches
import io.mockative.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OsmNoteQuestControllerTest {

    @Mock
    private lateinit var noteSource: NotesWithEditsSource
    @Mock
    private lateinit var hiddenDB: NoteQuestsHiddenDao
    @Mock
    private lateinit var userDataSource: UserDataSource
    @Mock
    private lateinit var userLoginStatusSource: UserLoginStatusSource
    @Mock
    private lateinit var prefs: ObservableSettings

    private lateinit var ctrl: OsmNoteQuestController
    @Mock
    private lateinit var listener: OsmNoteQuestSource.Listener
    @Mock
    private lateinit var hideListener: OsmNoteQuestsHiddenSource.Listener

    private lateinit var noteUpdatesListener: NotesWithEditsSource.Listener
    private lateinit var userLoginListener: UserLoginStatusSource.Listener

    @BeforeTest
    fun setUp() {
        noteSource = mock(classOf<NotesWithEditsSource>())
        hiddenDB = mock(classOf<NoteQuestsHiddenDao>())
        userDataSource = mock(classOf<UserDataSource>())
        userLoginStatusSource = mock(classOf<UserLoginStatusSource>())
        prefs = mock(classOf<ObservableSettings>())

        listener = mock(classOf<OsmNoteQuestSource.Listener>())
        hideListener = mock(classOf<OsmNoteQuestsHiddenSource.Listener>())

        every { noteSource.addListener(any()) }.invokes { arguments ->
            noteUpdatesListener = arguments[0] as NotesWithEditsSource.Listener
            Unit
        }

        every { userLoginStatusSource.addListener(any()) }.invokes { arguments ->
            userLoginListener = arguments[0] as UserLoginStatusSource.Listener
            Unit
        }

        ctrl = OsmNoteQuestController(
            noteSource,
            hiddenDB,
            userDataSource,
            userLoginStatusSource,
            prefs
        )
        ctrl.addListener(listener)
        ctrl.addListener(hideListener)
    }

    @Test
    fun hide() {
        val note = note(1)
        val ts = 123L

        every { hiddenDB.getTimestamp(1) }.returns(ts)
        every { noteSource.get(1) }.returns(note)

        ctrl.hide(1)

        verifyInvokedExactlyOnce { hiddenDB.add(1) }
        verifyInvokedExactlyOnce { hideListener.onHid(eq(OsmNoteQuestHidden(note, ts))) }
        verifyInvokedExactlyOnce {
            listener.onUpdated(
                addedQuests = eq(emptyList()),
                deletedQuestIds = eq(listOf(1))
            )
        }
    }

    @Test
    fun unhide() {
        val note = note(1)
        val ts = 123L

        every { hiddenDB.getTimestamp(1) }.returns(ts)
        every { noteSource.get(1) }.returns(note)
        every { hiddenDB.delete(1) }.returns(true)
        every { prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false) }.returns(true)

        ctrl.unhide(1)

        verifyInvokedExactlyOnce { hideListener.onUnhid(eq(OsmNoteQuestHidden(note, ts))) }
        verifyInvokedExactlyOnce {
            listener.onUpdated(
                addedQuests = eq(listOf(OsmNoteQuest(1, note.position))),
                deletedQuestIds = eq(emptyList())
            )
        }
    }

    @Test
    fun unhideAll() {
        val hiddenNoteIds = listOf<Long>(1, 2, 3)
        val hiddenNotes = listOf(
            note(1), note(2), note(3)
        )

        every { hiddenDB.getAllIds() }.returns(hiddenNoteIds)
        every { noteSource.getAll(hiddenNoteIds) }.returns(hiddenNotes)
        every { prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false) }.returns(true)

        ctrl.unhideAll()

        val expectedQuests = hiddenNotes.map { OsmNoteQuest(it.id, it.position) }

        verifyInvokedExactlyOnce { hiddenDB.deleteAll() }
        verifyInvokedExactlyOnce { hideListener.onUnhidAll() }
        verifyInvokedExactlyOnce {
            listener.onUpdated(
                addedQuests = eq(expectedQuests),
                deletedQuestIds = eq(emptyList())
            )
        }
    }

    @Test
    fun getAllHiddenNewerThan() {
        val note1 = note(1)
        val note2 = note(2)

        every { hiddenDB.getNewerThan(123L) }.returns(
            listOf(
                NoteIdWithTimestamp(1, 300),
                NoteIdWithTimestamp(2, 500),
                NoteIdWithTimestamp(3, 600), // missing note
            )
        )
        every { noteSource.getAll(eq(listOf(1L, 2L, 3L))) }.returns(listOf(note1, note2))

        assertEquals(
            listOf(
                OsmNoteQuestHidden(note1, 300),
                OsmNoteQuestHidden(note2, 500),
            ),
            ctrl.getAllHiddenNewerThan(123L)
        )
    }

    @Test
    fun countAll() {
        every { hiddenDB.countAll() }.returns(123L)
        assertEquals(123L, ctrl.countAll())
    }

    @Test
    fun `get hidden returns null`() {
        every { noteSource.get(1) }.returns(note(1))
        every { hiddenDB.contains(1) }.returns(true)
        assertNull(ctrl.getVisible(1))
    }

    @Test
    fun `get missing returns null`() {
        every { noteSource.get(1) }.returns(null)
        assertNull(ctrl.getVisible(1))
    }

    @Test
    fun `get note quest with comment from user returns null`() {
        every { noteSource.get(1) }.returns(
            note(
                comments = listOf(
                    comment(text = "test?", user = User(id = 100, "Blaubär")),
                    comment(text = "test", user = User(id = 1, "Blubbi"))
                )
            )
        )
        every { userDataSource.userId }.returns(1)

        assertNull(ctrl.getVisible(1))
    }

    @Test
    fun `get note quest with comment from user that contains a survey required marker returns non-null`() {
        every { noteSource.get(1) }.returns(
            note(
                comments = listOf(
                    comment(text = "test?", user = User(id = 100, "Blaubär")),
                    comment(text = "ok but #surveyme", user = User(id = 1, "Blubbi")),
                )
            )
        )
        every { userDataSource.userId }.returns(1)

        assertNotNull(ctrl.getVisible(1))
    }

    @Test
    fun `get quest not phrased as question returns null`() {
        every { noteSource.get(1) }.returns(
            note(
                comments = listOf(
                    comment(text = "test")
                )
            )
        )
        every { prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false) }.returns(false)

        assertNull(ctrl.getVisible(1))
    }

    @Test
    fun `get quest phrased as question returns non-null`() {
        every { noteSource.get(1) }.returns(
            note(
                1,
                position = p(1.0, 1.0),
                comments = listOf(comment(text = "test?"))
            )
        )
        every { prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false) }.returns(false)

        assertEquals(OsmNoteQuest(1, p(1.0, 1.0)), ctrl.getVisible(1))
    }

    @Test
    fun `get quest phrased as question in other scripts returns non-null`() {
        every { noteSource.get(1) }.returns(
            note(
                1,
                comments = listOf(comment(text = "Greek question mark: ;"))
            )
        )
        every { noteSource.get(2) }.returns(
            note(
                2,
                comments = listOf(comment(text = "semicolon: ;"))
            )
        )
        every { noteSource.get(3) }.returns(
            note(
                3,
                comments = listOf(comment(text = "mirrored question mark: ؟"))
            )
        )
        every { noteSource.get(4) }.returns(
            note(
                4,
                comments = listOf(comment(text = "Armenian question mark: ՞"))
            )
        )
        every { noteSource.get(5) }.returns(
            note(
                5,
                comments = listOf(comment(text = "Ethiopian question mark: ፧"))
            )
        )
        every { noteSource.get(6) }.returns(
            note(
                6,
                comments = listOf(comment(text = "Vai question mark: ꘏"))
            )
        )
        every { noteSource.get(7) }.returns(
            note(
                7,
                comments = listOf(comment(text = "full width question mark: ？"))
            )
        )
        every { prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false) }.returns(false)

        assertEquals(1, ctrl.getVisible(1)?.id)
        assertEquals(2, ctrl.getVisible(2)?.id)
        assertEquals(3, ctrl.getVisible(3)?.id)
        assertEquals(4, ctrl.getVisible(4)?.id)
        assertEquals(5, ctrl.getVisible(5)?.id)
        assertEquals(6, ctrl.getVisible(6)?.id)
        assertEquals(7, ctrl.getVisible(7)?.id)
    }

    @Test
    fun `get quest with comment containing survey required marker returns non-null`() {
        every { noteSource.get(1) }.returns(
            note(
                1,
                position = p(1.0, 1.0),
                comments = listOf(comment(text = "test #surveyme"))
            )
        )
        every { prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false) }.returns(false)

        assertEquals(OsmNoteQuest(1, p(1.0, 1.0)), ctrl.getVisible(1))
    }

    @Test
    fun `get quest not phrased as question returns non-null by preference`() {
        every { noteSource.get(1) }.returns(
            note(
                1,
                position = p(1.0, 1.0),
                comments = listOf(comment(text = "test"))
            )
        )
        every { prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false) }.returns(true)

        assertEquals(OsmNoteQuest(1, p(1.0, 1.0)), ctrl.getVisible(1))
    }

    // not doing all the tests for getAll again because it uses the same functions

    @Test
    fun getAll() {
        val bbox = bbox()
        val notes = listOf(note(1), note(2), note(3))

        every { hiddenDB.getAllIds() }.returns(emptyList())
        every { noteSource.getAll(bbox) }.returns(notes)
        every { prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false) }.returns(true)

        val expectedQuests = notes.map { OsmNoteQuest(it.id, it.position) }

        assertEquals(
            expectedQuests,
            ctrl.getAllVisibleInBBox(bbox)
        )
    }

    @Test
    fun `calls onInvalidated when logged in`() {
        userLoginListener.onLoggedIn()

        verifyInvokedExactlyOnce { listener.onInvalidated() }
    }

    @Test
    fun `calls onInvalidated when cleared notes`() {
        noteUpdatesListener.onCleared()
        verifyInvokedExactlyOnce { listener.onInvalidated() }
    }

    @Test
    fun `calls onUpdated when notes changed`() {
        // note 1 is added
        // note 2 is not eligible
        // note 3 is added/updated
        // note 4 is not eligible -> delete it
        // note 5 is deleted

        every { hiddenDB.getAllIds() }.returns(listOf(2, 4))
        every { prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false) }.returns(true)

        noteUpdatesListener.onUpdated(
            added = listOf(
                note(1),
                note(2)
            ),
            updated = listOf(note(3), note(4)),
            deleted = listOf(5)
        )

        verifyInvokedExactlyOnce {
            listener.onUpdated(
                addedQuests = matches {
                    it.containsExactlyInAnyOrder(
                        listOf(
                            OsmNoteQuest(1, p()),
                            OsmNoteQuest(3, p()),
                        )
                    )
                },
                deletedQuestIds = matches { it.containsExactlyInAnyOrder(listOf(4, 5)) }
            )
        }
    }
}
