package de.westnordost.streetcomplete.data.osm.upload

import org.junit.Before
import org.junit.Test


import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.*
import de.westnordost.osmapi.map.data.Element.Type.*
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.argumentCaptor
import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.OsmQuest
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryChange
import de.westnordost.streetcomplete.on
import org.junit.Assert.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*

open class SingleOsmQuestUploadTest {
    private lateinit var uploader: SingleOsmQuestUpload
    private lateinit var osmDao: MapDataDao
    private lateinit var quest: OsmQuest
    private lateinit var questType: OsmElementQuestType<*>

    private val nodeId: Long = 5
    private val pos = OsmLatLon(1.0, 2.0)
    private val node: Node = OsmNode(nodeId, 1, pos, mutableMapOf())

    @Before fun setUp() {
	    questType = mock(OsmElementQuestType::class.java)
	    on(questType.isApplicableTo(any())).thenReturn(true)

        osmDao = mock(MapDataDao::class.java)

	    quest = OsmQuest(1L, questType, NODE, nodeId, QuestStatus.ANSWERED, null,
		    "test case", null, ElementGeometry(pos))
	    quest.setChanges(StringMapEntryAdd("a key","a value"))

        uploader = SingleOsmQuestUpload(osmDao)
    }

	@Test fun `applies changes and uploads element`() {
		quest.setChanges(StringMapEntryAdd("test", "123"))

        val element = doUpload(quest, node)

        assertEquals(mapOf("test" to "123"), element.tags)
    }

	@Test fun `handles a solvable conflict`() {
		quest.setChanges(StringMapEntryAdd("a key", "a value"))

        reportConflictOnFirstUploadAttempt()
		on(osmDao.getNode(nodeId)).thenReturn(createNode(2, mapOf("another key" to "another value")))

        val element = doUpload(quest, node)

        assertEquals(
            mapOf("a key" to "a value", "another key" to "another value"),
            element.tags
        )
	}

    @Test fun `handles a conflict caused by negative version of element`() {
        quest.setChanges(StringMapEntryAdd("a key", "a value"))

        on(osmDao.getNode(nodeId)).thenReturn(createNode(1))

        val element = doUpload(quest, createNode(-1))

        assertEquals(mapOf("a key" to "a value"), element.tags)
        assertEquals(1, element.version)
    }

    @Test(expected = ElementDeletedException::class)
    fun `raise conflict when element was deleted`() {
        reportConflictOnFirstUploadAttempt()
        on(osmDao.getNode(nodeId)).thenReturn(null)

        doUpload(quest, node)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict when updated element changed the same tag`() {
	    // quest wants to add key=123, but the updated element already has key=abc
	    quest.setChanges(StringMapEntryAdd("key", "123"))

        reportConflictOnFirstUploadAttempt()
	    on(osmDao.getNode(nodeId)).thenReturn(createNode(2, mapOf("key" to "abc")))

        doUpload(quest, node)
	}

    @Test(expected = ElementConflictException::class)
	fun `raise conflict when a tag value of the change is too long`() {
		quest.setChanges(StringMapEntryAdd("too", "l"+"o".repeat(1000)+"ng"))

        doUpload(quest, node)
	}

    @Test(expected = ElementConflictException::class)
	fun `raise conflict when the updated element is no longer applicable to the quest`() {
        reportConflictOnFirstUploadAttempt()
		on(osmDao.getNode(nodeId)).thenReturn(createNode(2))
		on(questType.isApplicableTo(any())).thenReturn(false)

        doUpload(quest, node)
	}

    @Test fun `do not raise conflict when the quest is no longer applicable but is ignored by quest`() {
        reportConflictOnFirstUploadAttempt()
        on(osmDao.getNode(nodeId)).thenReturn(createNode(2))
        on(questType.isApplicableTo(any())).thenReturn(false)

        val dontCareQuest = object : OsmQuest(1L, questType, NODE, nodeId, QuestStatus.ANSWERED, null,
            "test case", null, ElementGeometry(pos)) {
            override fun isApplicableTo(element: Element?) = true
        }

        doUpload(dontCareQuest, node)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict when the updated node moved`() {
        val old = OsmNode(0, 0, OsmLatLon(51.4777, 0.0), null)
        val new = OsmNode(0, 1, OsmLatLon(51.4780, 0.0), null)
        attemptUploadButElementChanged(old, new)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict when the updated way was extended one one side`() {
        val old = OsmWay(0, 0, listOf(1,2), null)
        val new = OsmWay(0, 1, listOf(4,1,2), null)
        attemptUploadButElementChanged(old, new)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict when the updated way was extended one the other side`() {
        val old = OsmWay(0, 0, listOf(1,2), null)
        val new = OsmWay(0, 1, listOf(1,2,3), null)
        attemptUploadButElementChanged(old, new)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict when the updated way was shortened on one side`() {
        val old = OsmWay(0, 0, listOf(1,2,3), null)
        val new = OsmWay(0, 1, listOf(2,3), null)
        attemptUploadButElementChanged(old, new)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict when the updated way was shortened on the other side`() {
        val old = OsmWay(0, 0, listOf(1,2,3), null)
        val new = OsmWay(0, 1, listOf(1,2), null)
        attemptUploadButElementChanged(old, new)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict when the updated relation has different members`() {
        val old = OsmRelation(0, 0, listOf(OsmRelationMember(0, "outer", WAY)), null)
        val new = OsmRelation(0, 1, listOf(OsmRelationMember(0, "inner", WAY)), null)
        attemptUploadButElementChanged(old, new)
    }

    @Test fun `do not raise conflict when the updated way was extended not at the ends`() {
        val old = OsmWay(0, 0, listOf(1,2,3), null)
        val new = OsmWay(0, 1, listOf(1,2,4,5,6,3), null)
        attemptUploadButElementChanged(old, new)
    }

	@Test(expected = OsmConflictException::class)
    fun `do not catch a changeset conflict exception`() {
		// OSM Dao returns an element with the same version as in the database
        reportConflictOnFirstUploadAttempt()
        on(osmDao.getNode(anyLong())).thenReturn(node)

        doUpload(quest, node)
	}

    @Test(expected = ElementConflictException::class)
    fun `raise runtime exception if API continues to report conflict`() {
        on(osmDao.getNode(anyLong()))
            .thenReturn(createNode(2))
            .thenReturn(createNode(3))

        doThrow(OsmConflictException::class.java)
            .doThrow(OsmConflictException::class.java)
            .on(osmDao.uploadChanges(anyLong(), any(), any()))
    }

    private fun attemptUploadButElementChanged(element: Element, newElement: Element): Element {
        // the ElementGeometry does not actually matter here, so just use a simple value here
        val geometry = ElementGeometry(pos)
        quest = OsmQuest(1L, questType, element.type, element.id, QuestStatus.ANSWERED, null,
            "test case", null, geometry)
        quest.setChanges(StringMapEntryAdd("a key","a value"))

        when(element.type!!) {
            NODE -> on(osmDao.getNode(element.id)).thenReturn(newElement as Node)
            WAY -> on(osmDao.getWay(element.id)).thenReturn(newElement as Way)
            RELATION -> on(osmDao.getRelation(element.id)).thenReturn(newElement as Relation)
        }

        reportConflictOnFirstUploadAttempt()

        return doUpload(quest, element)
    }

    protected fun reportConflictOnFirstUploadAttempt() {
        doThrow(OsmConflictException::class.java).doNothing().on(osmDao).uploadChanges(anyLong(), any(), any())
    }

    protected fun createNode(version: Int, tags: Map<String, String>? = null) =
		OsmNode(nodeId, version, pos, tags)

    protected fun OsmQuest.setChanges(vararg changes: StringMapEntryChange) {
		setChanges(StringMapChanges(changes.asList()), "test")
	}

    private fun doUpload(q: OsmQuest = quest, e: Element = node) : Element {
        uploader.upload(0L, q, e)
        val arg: ArgumentCaptor<Iterable<Element>> = argumentCaptor()
        verify(osmDao).uploadChanges(eq(0L), arg.capture(), any())
        val elements = arg.value.toList()
        assertEquals(1, elements.size)
        val element = elements.single()
        assertTrue(element.isModified)
        return element
    }
}
