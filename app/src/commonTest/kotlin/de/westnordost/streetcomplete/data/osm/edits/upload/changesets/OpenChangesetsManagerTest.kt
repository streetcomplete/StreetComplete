package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.answering.returns
import dev.mokkery.every
import de.westnordost.streetcomplete.util.math.translate
import dev.mokkery.everySuspend
import kotlinx.coroutines.runBlocking
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OpenChangesetsManagerTest {

    private lateinit var questType: OsmElementQuestType<*>
    private lateinit var changesetApiClient: ChangesetApiClient
    private lateinit var openChangesetsDB: OpenChangesetsDao
    private lateinit var changesetAutoCloser: ChangesetAutoCloser
    private lateinit var manager: OpenChangesetsManager
    private lateinit var prefs: Preferences

    @BeforeTest fun setUp() {
        questType = TestQuestTypeA()
        changesetApiClient = mock()
        openChangesetsDB = mock()
        changesetAutoCloser = mock()
        prefs = mock()
        manager = OpenChangesetsManager(changesetApiClient, openChangesetsDB, changesetAutoCloser, prefs)
    }

    @Test fun `create new changeset if none exists`(): Unit = runBlocking {
        every { openChangesetsDB.get(any(), any()) } returns null
        everySuspend { changesetApiClient.open(any()) } returns 123L

        assertEquals(123L, manager.getOrCreateChangeset(questType, "my source", LatLon(0.0, 0.0), false))

        verifySuspend { changesetApiClient.open(any()) }
        verify { openChangesetsDB.put(any()) }
    }

    @Test fun `reuse changeset if one exists`(): Unit = runBlocking {
        every { openChangesetsDB.get(questType.name, "source") } returns
            OpenChangeset(questType.name, "source", 123, LatLon(0.0, 0.0))

        assertEquals(123L, manager.getOrCreateChangeset(questType, "source", LatLon(0.0, 0.0), false))

        verifySuspend(not) { changesetApiClient.open(any()) }
    }

    @Test fun `reuse changeset if one exists and position is far away but should not create new if too far away`(): Unit = runBlocking {
        val p0 = LatLon(0.0, 0.0)
        val p1 = p0.translate(5001.0, 0.0)

        every { openChangesetsDB.get(questType.name, "source") }returns
            OpenChangeset(questType.name, "source", 123, p0)

        assertEquals(123L, manager.getOrCreateChangeset(questType, "source", p1, false))

        verifySuspend(not) { changesetApiClient.open(any()) }
    }

    @Test fun `close changeset and create new if one exists and position is far away`(): Unit = runBlocking {
        val p0 = LatLon(0.0, 0.0)
        val p1 = p0.translate(5001.0, 0.0)

        every { openChangesetsDB.get(questType.name, "source") } returns
            OpenChangeset(questType.name, "source", 123, p0)
        everySuspend { changesetApiClient.open(any()) } returns 124L

        assertEquals(124L, manager.getOrCreateChangeset(questType, "source", p1, true))
        verifySuspend { changesetApiClient.close(123L) }
        verifySuspend { changesetApiClient.open(any()) }
        verify { openChangesetsDB.delete(questType.name, "source") }
        verify { openChangesetsDB.put(OpenChangeset(questType.name, "source", 124L, p1)) }
    }

    @Test fun `create correct changeset tags`(): Unit = runBlocking {
        every { openChangesetsDB.get(any(), any()) } returns null
        everySuspend { changesetApiClient.open(any()) } returns 1

        manager.getOrCreateChangeset(questType, "my source", LatLon(0.0, 0.0), false)

        verifySuspend {
            changesetApiClient.open(mapOf(
                "source" to "my source",
                "created_by" to ApplicationConstants.USER_AGENT,
                "comment" to "test me",
                "locale" to Locale.current.toLanguageTag(),
                "StreetComplete:quest_type" to questType.name
            ))
        }
        verify { openChangesetsDB.put(any()) }
    }
}
