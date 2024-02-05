package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.edits.upload.LastEditTimeStore
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.util.math.translate
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import java.util.Locale
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OpenChangesetsManagerTest {

    private lateinit var questType: OsmElementQuestType<*>
    private lateinit var mapDataApi: MapDataApi
    private lateinit var openChangesetsDB: OpenChangesetsDao
    private lateinit var changesetAutoCloser: ChangesetAutoCloser
    private lateinit var manager: OpenChangesetsManager
    private lateinit var lastEditTimeStore: LastEditTimeStore

    @BeforeTest fun setUp() {
        questType = TestQuestTypeA()
        mapDataApi = mock()
        openChangesetsDB = mock()
        changesetAutoCloser = mock()
        lastEditTimeStore = mock()
        manager = OpenChangesetsManager(mapDataApi, openChangesetsDB, changesetAutoCloser, lastEditTimeStore)
    }

    @Test fun `create new changeset if none exists`() {
        on(openChangesetsDB.get(any(), any())).thenReturn(null)
        on(mapDataApi.openChangeset(any())).thenReturn(123L)

        assertEquals(123L, manager.getOrCreateChangeset(questType, "my source", LatLon(0.0, 0.0), false))

        verify(mapDataApi).openChangeset(any())
        verify(openChangesetsDB).put(any())
    }

    @Test fun `reuse changeset if one exists`() {
        on(openChangesetsDB.get(questType.name, "source")).thenReturn(
            OpenChangeset(questType.name, "source", 123, LatLon(0.0, 0.0))
        )

        assertEquals(123L, manager.getOrCreateChangeset(questType, "source", LatLon(0.0, 0.0), false))

        verify(mapDataApi, never()).openChangeset(any())
    }

    @Test fun `reuse changeset if one exists and position is far away but should not create new if too far away`() {
        val p0 = LatLon(0.0, 0.0)
        val p1 = p0.translate(5001.0, 0.0)

        on(openChangesetsDB.get(questType.name, "source")).thenReturn(
            OpenChangeset(questType.name, "source", 123, p0)
        )
        assertEquals(123L, manager.getOrCreateChangeset(questType, "source", p1, false))
        verify(mapDataApi, never()).openChangeset(any())
    }

    @Test fun `close changeset and create new if one exists and position is far away`() {
        val p0 = LatLon(0.0, 0.0)
        val p1 = p0.translate(5001.0, 0.0)

        on(openChangesetsDB.get(questType.name, "source")).thenReturn(
            OpenChangeset(questType.name, "source", 123, p0)
        )
        on(mapDataApi.openChangeset(any())).thenReturn(124L)

        assertEquals(124L, manager.getOrCreateChangeset(questType, "source", p1, true))
        verify(mapDataApi).closeChangeset(123L)
        verify(mapDataApi).openChangeset(any())
        verify(openChangesetsDB).delete(questType.name, "source")
        verify(openChangesetsDB).put(OpenChangeset(questType.name, "source", 124L, p1))
    }

    @Test fun `create correct changeset tags`() {
        on(openChangesetsDB.get(any(), any())).thenReturn(null)
        val locale = Locale.getDefault()
        Locale.setDefault(Locale("es", "AR"))

        manager.getOrCreateChangeset(questType, "my source", LatLon(0.0, 0.0), false)

        Locale.setDefault(locale)

        verify(mapDataApi).openChangeset(mapOf(
            "source" to "my source",
            "created_by" to ApplicationConstants.USER_AGENT,
            "comment" to "test me",
            "locale" to "es-AR",
            "StreetComplete:quest_type" to questType.name
        ))
        verify(openChangesetsDB).put(any())
    }
}
