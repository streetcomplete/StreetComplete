package de.westnordost.streetcomplete.data.osm.upload

import android.content.SharedPreferences
import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.changesets.OpenChangeset
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.util.*

class OpenQuestChangesetsManagerTest {

    private lateinit var questType: OsmElementQuestType<*>
    private lateinit var osmDao: MapDataDao
    private lateinit var openChangesetsDB: OpenChangesetsDao
    private lateinit var changesetAutoCloser: ChangesetAutoCloser
    private lateinit var manager: OpenQuestChangesetsManager
    private lateinit var prefs: SharedPreferences

    @Before fun setUp() {
        questType = TestQuestType()
        osmDao = mock()
        openChangesetsDB = mock()
        changesetAutoCloser = mock()
        prefs = mock()
        manager = OpenQuestChangesetsManager(osmDao, openChangesetsDB, changesetAutoCloser, prefs)
    }

    @Test fun `create new changeset if none exists`() {
        on(openChangesetsDB.get(any(), any())).thenReturn(null)
        on(osmDao.openChangeset(any())).thenReturn(123L)

        assertEquals(123L, manager.getOrCreateChangeset(questType, "my source"))

        verify(osmDao).openChangeset(any())
        verify(openChangesetsDB).put(any())
    }

    @Test fun `reuse changeset if one exists`() {
        on(openChangesetsDB.get(any(), any())).thenReturn(OpenChangeset("bla", "source", 123))

        assertEquals(123L, manager.getOrCreateChangeset(questType, "my source"))

        verify(osmDao, never()).openChangeset(any())
    }

    @Test fun `create correct changeset tags`() {
        on(openChangesetsDB.get(any(), any())).thenReturn(null)
        val locale = Locale.getDefault()
        Locale.setDefault(Locale("es", "AR"))

        manager.getOrCreateChangeset(questType, "my source")

        Locale.setDefault(locale)

        verify(osmDao).openChangeset(mapOf(
            "source" to "my source",
            "created_by" to ApplicationConstants.USER_AGENT,
            "comment" to "test me",
            "locale" to "es-AR",
            "StreetComplete:quest_type" to "TestQuestType"
        ))
        verify(openChangesetsDB).put(any())
    }

}

private class TestQuestType : OsmElementQuestType<String> {

    override fun getTitle(tags: Map<String, String>) = 0
    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit) = false
    override fun isApplicableTo(element: Element):Boolean? = null
    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {}
    override val icon = 0
    override fun createForm(): AbstractQuestAnswerFragment<String> = object : AbstractQuestAnswerFragment<String>() {}
    override val commitMessage = "test me"
}

