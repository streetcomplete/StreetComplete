package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.edits.upload.LastEditTimeStore
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.testutils.verifyInvokedExactly
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import de.westnordost.streetcomplete.util.math.translate
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import java.util.Locale
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OpenChangesetsManagerTest {

    @Mock private lateinit var questType: OsmElementQuestType<*>
    @Mock private lateinit var mapDataApi: MapDataApi
    @Mock private lateinit var openChangesetsDB: OpenChangesetsDao
    @Mock private lateinit var changesetAutoCloser: ChangesetAutoCloser
    private lateinit var manager: OpenChangesetsManager
    @Mock private lateinit var lastEditTimeStore: LastEditTimeStore

    @BeforeTest fun setUp() {
        questType = TestQuestTypeA()
        mapDataApi = mock(classOf<MapDataApi>())
        openChangesetsDB = mock(classOf<OpenChangesetsDao>())
        changesetAutoCloser = mock(classOf<ChangesetAutoCloser>())
        lastEditTimeStore = mock(classOf<LastEditTimeStore>())
        manager = OpenChangesetsManager(mapDataApi, openChangesetsDB, changesetAutoCloser, lastEditTimeStore)
    }

    @Test fun `create new changeset if none exists`() {
        every { openChangesetsDB.get(any(), any()) }.returns(null)
        every { mapDataApi.openChangeset(any()) }.returns(123L)

        assertEquals(123L, manager.getOrCreateChangeset(questType, "my source", LatLon(0.0, 0.0), false))

        verifyInvokedExactlyOnce { mapDataApi.openChangeset(any()) }
        verifyInvokedExactlyOnce { openChangesetsDB.put(any()) }
    }

    @Test fun `reuse changeset if one exists`() {
        every { openChangesetsDB.get(questType.name, "source") }.returns(
            OpenChangeset(questType.name, "source", 123, LatLon(0.0, 0.0))
        )

        assertEquals(123L, manager.getOrCreateChangeset(questType, "source", LatLon(0.0, 0.0), false))

        verifyInvokedExactly(0) { mapDataApi.openChangeset(any()) }
    }

    @Test fun `reuse changeset if one exists and position is far away but should not create new if too far away`() {
        val p0 = LatLon(0.0, 0.0)
        val p1 = p0.translate(5001.0, 0.0)

        every { openChangesetsDB.get(questType.name, "source") }.returns(
            OpenChangeset(questType.name, "source", 123, p0)
        )
        assertEquals(123L, manager.getOrCreateChangeset(questType, "source", p1, false))
        verifyInvokedExactly(0) { mapDataApi.openChangeset(any()) }
    }

    @Test fun `close changeset and create new if one exists and position is far away`() {
        val p0 = LatLon(0.0, 0.0)
        val p1 = p0.translate(5001.0, 0.0)

        every { openChangesetsDB.get(questType.name, "source") }.returns(
            OpenChangeset(questType.name, "source", 123, p0)
        )
        every { mapDataApi.openChangeset(any()) }.returns(124L)
        every { openChangesetsDB.delete(questType.name, "source") }.returns(true)

        assertEquals(124L, manager.getOrCreateChangeset(questType, "source", p1, true))
        verifyInvokedExactlyOnce { mapDataApi.closeChangeset(123L) }
        verifyInvokedExactlyOnce { mapDataApi.openChangeset(any()) }
        verifyInvokedExactlyOnce { openChangesetsDB.delete(questType.name, "source") }
        verifyInvokedExactlyOnce { openChangesetsDB.put(OpenChangeset(questType.name, "source", 124L, p1)) }
    }

    @Test fun `create correct changeset tags`() {
        every { openChangesetsDB.get(any(), any()) }.returns(null)
        every { mapDataApi.openChangeset(any()) }.returns(123L)
        val locale = Locale.getDefault()
        Locale.setDefault(Locale("es", "AR"))

        manager.getOrCreateChangeset(questType, "my source", LatLon(0.0, 0.0), false)

        Locale.setDefault(locale)

        verifyInvokedExactlyOnce {(mapDataApi).openChangeset(mapOf(
            "source" to "my source",
            "created_by" to ApplicationConstants.USER_AGENT,
            "comment" to "test me",
            "locale" to "es-AR",
            "StreetComplete:quest_type" to questType.name
        ))}
        verifyInvokedExactlyOnce { openChangesetsDB.put(any()) }
    }
}
