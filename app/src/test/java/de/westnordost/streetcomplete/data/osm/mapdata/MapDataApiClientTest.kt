package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.QueryTooBigException
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetApiClient
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetApiSerializer
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.testutils.OsmDevApi
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.way
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapDataApiClientTest {

    private val allowEverything = mock<UserLoginSource>()
    private val allowNothing = mock<UserLoginSource>()
    private val anonymous = mock<UserLoginSource>()

    init {
        on(allowEverything.accessToken).thenReturn(OsmDevApi.ALLOW_EVERYTHING_TOKEN)
        on(allowNothing.accessToken).thenReturn(OsmDevApi.ALLOW_NOTHING_TOKEN)
        on(anonymous.accessToken).thenReturn(null)
    }

    @Test fun getNode(): Unit = runBlocking {
        assertEquals("Yangon", liveClient.getNode(NDDE_YANGON)?.tags?.get("name:en"))
        assertNull(liveClient.getNode(0))
    }

    @Test fun getWay(): Unit = runBlocking {
        assertEquals("Oderhafen", liveClient.getWay(WAY_ODERHAFEN)?.tags?.get("name"))
        assertNull(liveClient.getWay(0))
    }

    @Test fun getRelation(): Unit = runBlocking {
        assertEquals("Hamburg", liveClient.getRelation(RELATION_HAMBURG)?.tags?.get("name"))
        assertNull(liveClient.getRelation(0))
    }

    @Test fun getWaysForNode(): Unit = runBlocking {
        assertTrue(liveClient.getWaysForNode(VERTEX_OF_ELBPHILHARMONIE).isNotEmpty())
        assertTrue(liveClient.getWaysForNode(0).isEmpty())
    }

    @Test fun getRelationsForNode(): Unit = runBlocking {
        assertTrue(liveClient.getRelationsForNode(NODE_BUS_STATION).isNotEmpty())
        assertTrue(liveClient.getRelationsForNode(0).isEmpty())
    }

    @Test fun getRelationsForWay(): Unit = runBlocking {
        assertTrue(liveClient.getRelationsForWay(WAY_NEAR_BUS_STATION).isNotEmpty())
        assertTrue(liveClient.getRelationsForWay(0).isEmpty())
    }

    @Test fun getRelationsForRelation(): Unit = runBlocking {
        assertTrue(liveClient.getRelationsForRelation(RELATION_ONE_WAY_OF_BUS_ROUTE).isNotEmpty())
        assertTrue(liveClient.getRelationsForRelation(0).isEmpty())
    }

    @Test fun getWayComplete(): Unit = runBlocking {
        val data = liveClient.getWayComplete(WAY_ODERHAFEN)
        assertNotNull(data)
        assertTrue(data.nodes.isNotEmpty())
        assertTrue(data.ways.size == 1)

        assertNull(liveClient.getWayComplete(0))
    }

    @Test fun getRelationComplete(): Unit = runBlocking {
        val data = liveClient.getRelationComplete(RELATION_HAMBURG)
        assertNotNull(data)
        assertTrue(data.nodes.isNotEmpty())
        assertTrue(data.ways.isNotEmpty())

        assertNull(liveClient.getRelationComplete(0))
    }

    @Test fun getMap(): Unit = runBlocking {
        val hamburg = liveClient.getMap(HAMBURG_CITY_AREA)
        assertTrue(hamburg.nodes.isNotEmpty())
        assertTrue(hamburg.ways.isNotEmpty())
        assertTrue(hamburg.relations.isNotEmpty())
    }

    @Test fun `getMap does not return relations of ignored type`(): Unit = runBlocking {
        val hamburg = liveClient.getMap(AREA_NEAR_BUS_STATION, setOf("route"))
        assertTrue(hamburg.relations.none { it.tags["type"] == "route" })
    }

    @Test fun `getMap fails when bbox crosses 180th meridian`(): Unit = runBlocking {
        assertFailsWith<IllegalArgumentException> {
            liveClient.getMap(BoundingBox(0.0, 179.9999999, 0.0000001, -179.9999999))
        }
    }

    @Test fun `getMap fails when bbox is too big`(): Unit = runBlocking {
        assertFailsWith<QueryTooBigException> {
            liveClient.getMap(BoundingBox(-90.0, -180.0, 90.0, 180.0))
        }
    }

    @Test fun `getMap returns bounding box that was specified in request`(): Unit = runBlocking {
        val hamburg = liveClient.getMap(HAMBURG_CITY_AREA)
        assertEquals(HAMBURG_CITY_AREA, hamburg.boundingBox)
    }

    @Test fun `uploadChanges as anonymous fails`(): Unit = runBlocking {
        assertFailsWith<AuthorizationException> {
            client(anonymous).uploadChanges(1L, MapDataChanges())
        }
    }

    @Test fun `uploadChanges without authorization fails`(): Unit = runBlocking {
        assertFailsWith<AuthorizationException> {
            client(allowNothing).uploadChanges(1L, MapDataChanges())
        }
    }

    @Test fun `uploadChanges in already closed changeset fails`(): Unit = runBlocking {
        val changesetId = changesetClient(allowEverything).open(mapOf())
        changesetClient(allowEverything).close(changesetId)
        assertFailsWith<ConflictException> {
            client(allowEverything).uploadChanges(changesetId, MapDataChanges())
        }
    }

    @Test fun `uploadChanges of non-existing element fails`(): Unit = runBlocking {
        val changesetId = changesetClient(allowEverything).open(mapOf())
        assertFailsWith<ConflictException> {
            client(allowEverything).uploadChanges(
                changesetId = changesetId,
                changes = MapDataChanges(modifications = listOf(node(Long.MAX_VALUE)))
            )
        }
        changesetClient(allowEverything).close(changesetId)
    }

    @Test fun uploadChanges(): Unit = runBlocking {
        val changesetId = changesetClient(allowEverything).open(mapOf())

        val updates1 = client(allowEverything).uploadChanges(
            changesetId = changesetId,
            changes = MapDataChanges(
                creations = listOf(
                    node(-1, pos = p(15.0, -39.0), tags = mapOf("first" to "1")),
                    node(-2, pos = p(15.0, -39.1), tags = mapOf("second" to "2")),
                    node(-3, pos = p(15.0, -39.1), tags = mapOf("third" to "3")),
                    way(-4, nodes = listOf(-1, -2, -3)),
                )
            )
        )
        assertEquals(
            setOf(
                ElementKey(ElementType.NODE, -1),
                ElementKey(ElementType.NODE, -2),
                ElementKey(ElementType.NODE, -3),
                ElementKey(ElementType.WAY, -4),
            ),
            updates1.idUpdates.map { ElementKey(it.elementType, it.oldElementId) }.toSet()
        )
        assertEquals(4, updates1.updated.size)

        val elements = updates1.updated
        val firstNode = elements.find { it.tags["first"] == "1" } as Node
        val secondNode = elements.find { it.tags["second"] == "2" } as Node
        val thirdNode = elements.find { it.tags["third"] == "3" } as Node
        val way = elements.filterIsInstance<Way>().single()

        val updates2 = client(allowEverything).uploadChanges(
            changesetId = changesetId,
            changes = MapDataChanges(
                modifications = listOf(way.copy(nodeIds = listOf(secondNode.id, thirdNode.id))),
                deletions = listOf(firstNode),
            )
        )
        assertTrue(updates2.idUpdates.isEmpty())
        assertEquals(listOf(firstNode.key), updates2.deleted)
        assertEquals(listOf(way.key), updates2.updated.map { it.key })

        changesetClient(allowEverything).close(changesetId)
    }

    private fun client(userLoginSource: UserLoginSource) =
        MapDataApiClient(HttpClient(), OsmDevApi.URL, userLoginSource, MapDataApiParser(), MapDataApiSerializer())

    private fun changesetClient(userLoginSource: UserLoginSource) =
        ChangesetApiClient(HttpClient(), OsmDevApi.URL, userLoginSource, ChangesetApiSerializer())

    private val liveClient =
        MapDataApiClient(HttpClient(), "https://api.openstreetmap.org/api/0.6/", anonymous, MapDataApiParser(), MapDataApiSerializer())

    // some elements that should exist on the live API

    private val NDDE_YANGON = 26576175L
    private val WAY_ODERHAFEN = 23564402L
    private val RELATION_HAMBURG = 451087L

    private val VERTEX_OF_ELBPHILHARMONIE = 271454735L

    private val NODE_BUS_STATION = 483688378L
    private val WAY_NEAR_BUS_STATION = 148796410L
    private val RELATION_ONE_WAY_OF_BUS_ROUTE = 36912L

    private val AREA_NEAR_BUS_STATION = BoundingBox(53.6068315, 9.9046576, 53.6079471, 9.9062240)
    private val HAMBURG_CITY_AREA = BoundingBox(53.579, 9.939, 53.580, 9.940)
}
