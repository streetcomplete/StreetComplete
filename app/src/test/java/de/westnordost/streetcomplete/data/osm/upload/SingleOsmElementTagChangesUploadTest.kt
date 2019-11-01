package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.osmapi.common.Handler
import org.junit.Before
import org.junit.Test


import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.changes.DiffElement
import de.westnordost.osmapi.map.data.*
import de.westnordost.osmapi.map.data.Element.Type.*
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.argumentCaptor
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryChange
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Assert.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*
import org.mockito.invocation.InvocationOnMock

class SingleOsmElementTagChangesUploadTest {
    private lateinit var uploader: SingleOsmElementTagChangesUpload
    private lateinit var osmDao: MapDataDao
    private lateinit var quest: HasElementTagChanges

    private val nodeId: Long = 5
    private val pos = OsmLatLon(1.0, 2.0)
    private val node: Node = OsmNode(nodeId, 1, pos, mutableMapOf())

    @Before fun setUp() {
        osmDao = mock()

        quest = mock()
        on(quest.changes).thenReturn(changes(StringMapEntryAdd("a key","a value")))
        on(quest.isApplicableTo(any())).thenReturn(true)

        uploader = SingleOsmElementTagChangesUpload(osmDao)
    }

    @Test fun `applies changes and uploads element`() {
        on(quest.changes).thenReturn(changes(StringMapEntryAdd("test","123")))
        willUploadSuccessfully(node)

        uploader.upload(0L, quest, node)

        val element = getUploadedElement()
        assertEquals(mapOf("test" to "123"), element.tags)
    }

    @Test fun `handles a solvable conflict`() {
        on(quest.changes).thenReturn(changes(StringMapEntryAdd("a key","a value")))

        val newNode = createNode(2, mapOf("another key" to "another value"))
        on(osmDao.getNode(nodeId)).thenReturn(newNode)
        reportConflictOnFirstUploadButThenUploadSuccessfully(newNode)

        uploader.upload(0L, quest, node)

        val element = getUploadedElement(2)
        assertEquals(
            mapOf("a key" to "a value", "another key" to "another value"),
            element.tags
        )
    }

    @Test fun `handles a conflict caused by negative version of element`() {
        on(quest.changes).thenReturn(changes(StringMapEntryAdd("a key","a value")))

        on(osmDao.getNode(nodeId)).thenReturn(createNode(1))
        willUploadSuccessfully(node)

        uploader.upload(0L, quest, createNode(-1))

        val element = getUploadedElement()
        assertEquals(mapOf("a key" to "a value"), element.tags)
        assertEquals(1, element.version)
    }

    @Test(expected = ElementDeletedException::class)
    fun `raise conflict when element was deleted`() {
        reportConflictOnUpload()
        on(osmDao.getNode(nodeId)).thenReturn(null)

        uploader.upload(0L, quest, node)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict when updated element changed the same tag`() {
        // quest wants to add key=123, but the updated element already has key=abc
        on(quest.changes).thenReturn(changes(StringMapEntryAdd("key","123")))

        reportConflictOnUpload()
        on(osmDao.getNode(nodeId)).thenReturn(createNode(2, mapOf("key" to "abc")))

        uploader.upload(0L, quest, node)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict when a tag value of the change is too long`() {
        on(quest.changes).thenReturn(changes(StringMapEntryAdd("too","l"+"o".repeat(1000)+"ng")))

        uploader.upload(0L, quest, node)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict when the updated element is no longer applicable to the quest`() {
        reportConflictOnUpload()
        on(osmDao.getNode(nodeId)).thenReturn(createNode(2))
        on(quest.isApplicableTo(any())).thenReturn(false)

        uploader.upload(0L, quest, node)
    }

    @Test fun `do not raise conflict when the quest is no longer applicable but is ignored by quest`() {
        val newNode = createNode(2)
        on(osmDao.getNode(nodeId)).thenReturn(newNode)
        on(quest.isApplicableTo(any())).thenReturn(false)
        reportConflictOnFirstUploadButThenUploadSuccessfully(newNode)

        val dontCareQuest = object : HasElementTagChanges {
            override var changes = changes(StringMapEntryAdd("a", "b"))
            override fun isApplicableTo(element: Element) = true
        }

        uploader.upload(0L, dontCareQuest, node)

        getUploadedElement(2)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict when the updated node moved`() {
        val old = OsmNode(0, 0, OsmLatLon(51.4777, 0.0), mutableMapOf())
        val new = OsmNode(0, 1, OsmLatLon(51.4780, 0.0), mutableMapOf())
        reportConflictOnUpload()
        on(osmDao.getNode(old.id)).thenReturn(new)

        uploader.upload(0L, quest, old)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict when the updated way was extended one one side`() {
        val old = OsmWay(0, 0, listOf(1,2), mutableMapOf())
        val new = OsmWay(0, 1, listOf(4,1,2), mutableMapOf())
        reportConflictOnUpload()
        on(osmDao.getWay(old.id)).thenReturn(new)

        uploader.upload(0L, quest, old)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict when the updated way was extended one the other side`() {
        val old = OsmWay(0, 0, listOf(1,2), mutableMapOf())
        val new = OsmWay(0, 1, listOf(1,2,3), mutableMapOf())
        reportConflictOnUpload()
        on(osmDao.getWay(old.id)).thenReturn(new)

        uploader.upload(0L, quest, old)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict when the updated way was shortened on one side`() {
        val old = OsmWay(0, 0, listOf(1,2,3), mutableMapOf())
        val new = OsmWay(0, 1, listOf(2,3), mutableMapOf())
        reportConflictOnUpload()
        on(osmDao.getWay(old.id)).thenReturn(new)

        uploader.upload(0L, quest, old)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict when the updated way was shortened on the other side`() {
        val old = OsmWay(0, 0, listOf(1,2,3), mutableMapOf())
        val new = OsmWay(0, 1, listOf(1,2), mutableMapOf())
        reportConflictOnUpload()
        on(osmDao.getWay(old.id)).thenReturn(new)

        uploader.upload(0L, quest, old)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict when the updated relation has different members`() {
        val old = OsmRelation(0, 0, listOf(OsmRelationMember(0, "outer", WAY)), mutableMapOf())
        val new = OsmRelation(0, 1, listOf(OsmRelationMember(0, "inner", WAY)), mutableMapOf())
        reportConflictOnUpload()
        on(osmDao.getRelation(old.id)).thenReturn(new)

        uploader.upload(0L, quest, old)
    }

    @Test fun `do not raise conflict when the updated way was extended not at the ends`() {
        val old = OsmWay(0, 0, listOf(1,2,3), mutableMapOf())
        val new = OsmWay(0, 1, listOf(1,2,4,5,6,3), mutableMapOf())
        on(osmDao.getWay(old.id)).thenReturn(new)
        reportConflictOnFirstUploadButThenUploadSuccessfully(new)

        uploader.upload(0L, quest, old)
    }

    @Test(expected = ChangesetConflictException::class)
    fun `do not catch a changeset conflict exception`() {
        // OSM Dao returns an element with the same version as in the database
        reportConflictOnUpload()
        on(osmDao.getNode(anyLong())).thenReturn(node)

        uploader.upload(0L, quest, node)
    }

    @Test(expected = ElementConflictException::class)
    fun `raise runtime exception if API continues to report conflict`() {
        on(osmDao.getNode(anyLong())).thenReturn(createNode(2), createNode(3))

        doThrow(OsmConflictException::class.java, OsmConflictException::class.java)
            .on(osmDao).uploadChanges(anyLong(), any(), any())

        uploader.upload(0L, quest, node)
    }

    /* shortcuts for mocking */

    private fun reportConflictOnUpload() {
        doThrow(OsmConflictException::class.java).on(osmDao).uploadChanges(anyLong(), any(), any())
    }

    private fun willUploadSuccessfully(e: Element) {
        doAnswer(handleDiffElement(e)).on(osmDao).uploadChanges(eq(0L), any(), any())
    }

    private fun reportConflictOnFirstUploadButThenUploadSuccessfully(e: Element) {
        doThrow(OsmConflictException::class.java).doAnswer(handleDiffElement(e))
            .on(osmDao).uploadChanges(anyLong(), any(), any())
    }

    private fun changes(vararg change: StringMapEntryChange) = StringMapChanges(change.toList())

    private fun createNode(version: Int, tags: Map<String, String>? = mutableMapOf()) =
        OsmNode(nodeId, version, pos, tags)

    private fun getUploadedElement(calls: Int = 1): Element {
        val elementsArg: ArgumentCaptor<Iterable<Element>> = argumentCaptor()
        verify(osmDao, times(calls)).uploadChanges(eq(0L), elementsArg.capture(), any())
        val elements = elementsArg.value.toList()
        assertEquals(1, elements.size)
        val element = elements.single()
        assertTrue(element.isModified)
        return element
    }

    /* mocking ugliness, don't look! */

    private fun handleDiffElement(e: Element): (InvocationOnMock) -> Unit =
        { invocation ->
            val handler = (invocation.arguments[2] as Handler<DiffElement>)
            handler.handle(e.createDiffElement())
        }

    private fun Element.createDiffElement() = DiffElement().also {
        it.clientId = id
        it.serverId = id
        it.serverVersion = version + 1
        it.type = type
    }
}
