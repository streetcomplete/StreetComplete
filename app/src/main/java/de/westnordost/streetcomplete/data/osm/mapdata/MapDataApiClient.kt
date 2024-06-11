package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.QueryTooBigException
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.data.wrapApiClientExceptions
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode

// TODO tests

/** Get and upload changes to map data */
class MapDataApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val userLoginSource: UserLoginSource,
    private val serializer: MapDataApiSerializer,
) : MapDataRepository {

    /**
     * Upload changes into an opened changeset.
     *
     * @param changesetId id of the changeset to upload changes into
     * @param changes changes to upload.
     *
     * @throws ConflictException if the changeset has already been closed, there is a conflict for
     *                           the elements being uploaded or the user who created the changeset
     *                           is not the same as the one uploading the change
     * @throws AuthorizationException if the application does not have permission to edit the map
     *                                (OAuth scope "write_api")
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the updated elements
     */
    suspend fun uploadChanges(
        changesetId: Long,
        changes: MapDataChanges,
        ignoreRelationTypes: Set<String?> = emptySet()
    ) = wrapApiClientExceptions {

        // TODO handle errors

        try {
            val response = httpClient.post(baseUrl + "changeset/$changesetId/upload") {
                userLoginSource.accessToken?.let { bearerAuth(it) }
                setBody(serializer.serializeMapDataChanges(changes, changesetId))
                expectSuccess = true
            }
            val diff = serializer.parseElementsDiffs(response.body<String>())

            // TODO ignore relation types...
            val handler = UpdatedElementsHandler(ignoreRelationTypes)
            api.uploadChanges(changesetId, changes.toOsmApiElements()) {
                handler.handle(it.toDiffElement())
            }
            val allChangedElements = changes.creations + changes.modifications + changes.deletions
            handler.getElementUpdates(allChangedElements)
        } catch (e: OsmApiException) {
            throw ConflictException(e.message, e)
        }
    }

    /**
     * Returns the map data in the given bounding box.
     *
     * @param bounds rectangle in which to query map data. May not cross the 180th meridian. This is
     * usually limited at 0.25 square degrees. Check the server capabilities.
     * @param ignoreRelationTypes don't put any relations of the given types in the given mutableMapData
     *
     * @throws QueryTooBigException if the bounds area is too large or too many elements would be returned
     * @throws IllegalArgumentException if the bounds cross the 180th meridian.
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the map data
     */
    suspend fun getMap(
        bounds: BoundingBox,
        ignoreRelationTypes: Set<String?> = emptySet()
    ): NodesWaysRelations = wrapApiClientExceptions {
        if (bounds.crosses180thMeridian) {
            throw IllegalArgumentException("Bounding box crosses 180th meridian")
        }

        try {
            val response = httpClient.get(baseUrl + "map") {
                parameter("bbox", bounds.toOsmApiString())
                expectSuccess = true
            }
            // TODO ignore relation types relation.tags?.get("type")
            return serializer.parseMapData(response.body())
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.BadRequest) {
                throw QueryTooBigException(e.message, e)
            } else {
                throw e
            }
        }
    }

    /**
     * Returns the given way by id plus all its nodes or null if the way does not exist.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    override suspend fun getWayComplete(id: Long): NodesWaysRelations? =
        getMapDataOrNull("way/$id/full")

    /**
     * Returns the given relation by id plus all its members and all nodes of ways that are members
     * of the relation. Or null if the relation does not exist.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    override suspend fun getRelationComplete(id: Long): NodesWaysRelations? =
        getMapDataOrNull("relation/$id/full")

    /**
     * Return the given node by id or null if it doesn't exist
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    override suspend fun getNode(id: Long): Node? =
        getMapDataOrNull("node/$id")?.nodes?.single()

    /**
     * Return the given way by id or null if it doesn't exist
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    override suspend fun getWay(id: Long): Way? =
        getMapDataOrNull("way/$id")?.ways?.single()

    /**
     * Return the given relation by id or null if it doesn't exist
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    override suspend fun getRelation(id: Long): Relation? =
        getMapDataOrNull("relation/$id")?.relations?.single()

    /**
     * Return all ways in which the given node is used.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    override suspend fun getWaysForNode(id: Long): List<Way> =
        getMapDataOrNull("node/$id/ways")?.ways.orEmpty()

    /**
     * Return all relations in which the given node is used.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    override suspend fun getRelationsForNode(id: Long): List<Relation> =
        getMapDataOrNull("node/$id/relations")?.relations.orEmpty()

    /**
     * Return all relations in which the given way is used.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    override suspend fun getRelationsForWay(id: Long): List<Relation> =
        getMapDataOrNull("way/$id/relations")?.relations.orEmpty()

    /**
     * Return all relations in which the given relation is used.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    override suspend fun getRelationsForRelation(id: Long): List<Relation> =
        getMapDataOrNull("relation/$id/relations")?.relations.orEmpty()


    private suspend fun getMapDataOrNull(query: String): NodesWaysRelations? = wrapApiClientExceptions {
        try {
            val response = httpClient.get(baseUrl + query) { expectSuccess = true }
            return serializer.parseMapData(response.body())
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.Gone, HttpStatusCode.NotFound -> return null
                else -> throw e
            }
        }
    }
}

// TODO or use MapData?
data class NodesWaysRelations(
    val nodes: List<Node>,
    val ways: List<Way>,
    val relations: List<Relation>
)

/** Data class that contains the map data updates (updated elements, deleted elements, elements
 *  whose id have been updated) after the modifications have been uploaded */
data class MapDataUpdates(
    val updated: Collection<Element> = emptyList(),
    val deleted: Collection<ElementKey> = emptyList(),
    val idUpdates: Collection<ElementIdUpdate> = emptyList()
)

data class ElementIdUpdate(
    val elementType: ElementType,
    val oldElementId: Long,
    val newElementId: Long
)

/** Data class that contains a the request to create, modify elements and delete the given elements */
data class MapDataChanges(
    val creations: Collection<Element> = emptyList(),
    val modifications: Collection<Element> = emptyList(),
    val deletions: Collection<Element> = emptyList()
)

data class DiffElement(
    val type: ElementType,
    val clientId: Long,
    val serverId: Long? = null,
    val serverVersion: Int? = null
)
