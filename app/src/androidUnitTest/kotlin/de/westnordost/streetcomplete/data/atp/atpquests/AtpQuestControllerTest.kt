package de.westnordost.streetcomplete.data.atp.atpquests

import de.westnordost.streetcomplete.data.atp.atpquests.edits.AtpDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.mockito.Mockito.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class AtpQuestControllerTest {

    private lateinit var mapDataSource: MapDataWithEditsSource
    private lateinit var atpDataSource: AtpDataWithEditsSource
    private lateinit var noteSource: NotesWithEditsSource
    private lateinit var userDataSource: UserDataSource
    private lateinit var userLoginSource: UserLoginSource
    private lateinit var prefs: Preferences
    private lateinit var registry: QuestTypeRegistry

    private lateinit var ctrl: AtpQuestController
    private lateinit var listener: AtpQuestSource.Listener

    private lateinit var noteUpdatesListener: NotesWithEditsSource.Listener
    private lateinit var userLoginListener: UserLoginSource.Listener

    // TODO: adapt for ATP or delete
    @BeforeTest fun setUp() {
        noteSource = mock()
        mapDataSource = mock()
        atpDataSource = mock()
        userDataSource = mock()
        userLoginSource = mock()
        prefs = mock()
        registry = mock()

        listener = mock()

        on(noteSource.addListener(any())).then { invocation ->
            noteUpdatesListener = invocation.getArgument(0)
            Unit
        }

        on(userLoginSource.addListener(any())).then { invocation ->
            userLoginListener = invocation.getArgument(0)
            Unit
        }

        ctrl = AtpQuestController(mapDataSource, atpDataSource, noteSource, registry)
        ctrl.addListener(listener)
    }

    // TODO: adapt for ATP or delete
    @Test fun `get missing returns null`() {
        on(noteSource.get(1)).thenReturn(null)
        assertNull(ctrl.get(1))
    }

    // TODO: adapt for ATP or delete
    // not doing all the tests for getAll again because it uses the same functions

    // TODO: adapt for ATP or delete
    /*
    @Test fun getAll() {
        val bbox = bbox()
        val atpEntries = listOf(atpEntry(1), atpEntry(2), atpEntry(3))

        on(atpDataSource.getAll(bbox)).thenReturn(atpEntries)

        val expectedQuests = notes.map { CreateElementQuest(it.id, it.position) }

        assertEquals(
            expectedQuests,
            ctrl.getAllInBBox(bbox)
        )
    }
    */

    // is onCleared still needed? it got copied from notes test and interface TODO
    @Test fun `calls onInvalidated when cleared entries`() {
        noteUpdatesListener.onCleared()
        verify(listener).onInvalidated()
    }

    // TODO: adapt for ATP or delete (seems reasonable to test? but both updated notes and main onUpdated needs to be tested)
    /*
    @Test fun `calls onUpdated when notes changed`() {
        on(prefs.showAllNotes).thenReturn(true)

        noteUpdatesListener.onUpdated(
            added = listOf(note(1)),
            updated = listOf(note(2)),
            deleted = listOf(3)
        )

        verify(listener).onUpdated(
            added = argThat {
                it.containsExactlyInAnyOrder(listOf(OsmNoteQuest(1, p()), OsmNoteQuest(2, p())))
            },
            deleted = argThat { it.containsExactlyInAnyOrder(listOf(3)) }
        )
    }
    */

    @Test fun `isThereOsmAtpMatch matches despite capitalization difference`() {
        assertTrue(ctrl.isThereOsmAtpMatch(mapOf("name" to "ALDI"), mapOf("name" to "Aldi")))
    }

    @Test fun `isThereOsmAtpMatch rejects matches when nothing matches`() {
        assertFalse(ctrl.isThereOsmAtpMatch(
            mapOf("name" to "Foobar", "shop" to "convenience"),
            mapOf("name" to "Platypus", "shop" to "trade"))
        )
    }

    @Test fun `isThereOsmAtpMatch allows matches between similar shop types`() {
        assertTrue(ctrl.isThereOsmAtpMatch(
            mapOf("name" to "Tesco", "shop" to "convenience"),
            mapOf("name" to "Tesco", "shop" to "supermarket"))
        )
    }

    /*
    TODO: implement such improvements
    @Test fun `isThereOsmAtpMatch does not detect match on completely mismatching names, also if type matches`() {
        assertFalse(ctrl.isThereOsmAtpMatch(
            mapOf("name" to "Foobar", "shop" to "convenience"),
            mapOf("name" to "Platypus", "shop" to "convenience"))
        )
    }

    @Test fun `isThereOsmAtpMatch allows matches between similar shop types, also with subtle name differences`() {
        assertFalse(ctrl.isThereOsmAtpMatch(
            mapOf("name" to "Tesco", "shop" to "convenience"),
            mapOf("name" to "Tesco #123", "shop" to "supermarket"))
        )
    }
     */
    // TODO: see https://codeberg.org/matkoniecz/list_how_openstreetmap_can_be_improved_with_alltheplaces_data/src/branch/master/test_matching_logic.py for possible extension


    @Test fun `new AllThePlaces entries cause quest creation`() { // TODO - implement

    }

    @Test fun `new AllThePlaces entries with matching shop already results in no quest`() { // TODO - implement

    }

    @Test fun `new map data cause quest creation`() { // TODO - implement

    }

}
