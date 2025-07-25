package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.QueryTooBigException
import de.westnordost.streetcomplete.data.user.UserAccessTokenSource
import de.westnordost.streetcomplete.data.wrapApiClientExceptions
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.asSource
import kotlinx.io.buffered

/** Get and upload changes to map data */
class MapDataApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val userAccessTokenSource: UserAccessTokenSource,
    private val parser: MapDataApiParser,
    private val serializer: MapDataApiSerializer,
) {

    /**
     * Upload changes into an opened changeset.
     *
     * @param changesetId id of the changeset to upload changes into
     * @param changes changes to upload.
```suggestion
     * @param ignoreRelation omit any relations for which the given function returns true.
     *                       Such relations can still be referred to as relation members,
     *                       though, the relations themselves are just not included
     *
     * @throws ConflictException if the changeset has already been closed, there is a conflict for
     *                           the elements being uploaded or the user who created the changeset
     *                           is not the same as the one uploading the change
     * @throws AuthorizationException if the application does not have permission to edit the map
     *                                (OAuth scope "write_api")
     * @throws ChangesetTooLargeException when the [changes] don't fit into the changeset with the given
     *                               [changesetId] anymore.
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the updated elements
     */
    suspend fun uploadChanges(
        changesetId: Long,
        changes: MapDataChanges,
        ignoreRelation: (tags: Map<String, String>) -> Boolean = { false }
    ): MapDataUpdates = wrapApiClientExceptions {
        try {
            val response = httpClient.post(baseUrl + "changeset/$changesetId/upload") {
                userAccessTokenSource.accessToken?.let { bearerAuth(it) }
                setBody(serializer.serialize(changes, changesetId))
                expectSuccess = true
            }
            val source = response.bodyAsChannel().asSource().buffered()
            val updates = parser.parseElementUpdates(source)
            val changedElements = changes.creations + changes.modifications + changes.deletions
            return createMapDataUpdates(changedElements, updates, ignoreRelation)
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                // current element version is outdated or current changeset has been closed already
                HttpStatusCode.Conflict,
                // an element referred to by another element does not exist (anymore) or was redacted
                HttpStatusCode.PreconditionFailed,
                // some elements do not exist anymore as it was deleted
                HttpStatusCode.Gone,
                // some elements do not exist and never existed
                HttpStatusCode.NotFound -> {
                    throw ConflictException(e.message, e)
                }
                HttpStatusCode.PayloadTooLarge -> {
                    throw ChangesetTooLargeException(e.message, e)
                }
                else -> throw e
            }
        }
    }

    /**
     * Returns the map data in the given bounding box.
     *
     * @param bounds rectangle in which to query map data. May not cross the 180th meridian. This is
     * usually limited at 0.25 square degrees. Check the server capabilities.
     * @param ignoreRelation omit any relations for which the given function returns true.
     *                       Such relations can still be referred to as relation members,
     *                       though, the relations themselves are just not included
     *
     * @throws QueryTooBigException if the bounds area is too large or too many elements would be returned
     * @throws IllegalArgumentException if the bounds cross the 180th meridian.
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the map data
     */
    suspend fun getMap(
        bounds: BoundingBox,
        ignoreRelation: (tags: Map<String, String>) -> Boolean = { false }
    ): MutableMapData = wrapApiClientExceptions {
        if (bounds.crosses180thMeridian) {
            throw IllegalArgumentException("Bounding box crosses 180th meridian")
        }

        try {
            val response = httpClient.get(baseUrl + "map") {
                parameter("bbox", bounds.toOsmApiString())
                expectSuccess = true
            }
            val source = response.bodyAsChannel().asSource().buffered()
            return parser.parseMapData(source, ignoreRelation)
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
    suspend fun getWayComplete(id: Long): MapData? =
        getMapDataOrNull("way/$id/full")

    /**
     * Returns the given relation by id plus all its members and all nodes of ways that are members
     * of the relation. Or null if the relation does not exist.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getRelationComplete(id: Long): MapData? =
        getMapDataOrNull("relation/$id/full")

    /**
     * Return the given node by id or null if it doesn't exist
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getNode(id: Long): Node? =
        getMapDataOrNull("node/$id")?.nodes?.single()

    /**
     * Return the given way by id or null if it doesn't exist
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getWay(id: Long): Way? =
        getMapDataOrNull("way/$id")?.ways?.single()

    /**
     * Return the given relation by id or null if it doesn't exist
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getRelation(id: Long): Relation? =
        getMapDataOrNull("relation/$id")?.relations?.single()

    /**
     * Return all ways in which the given node is used.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getWaysForNode(id: Long): Collection<Way> =
        getMapDataOrNull("node/$id/ways")?.ways.orEmpty()

    /**
     * Return all relations in which the given node is used.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getRelationsForNode(id: Long): Collection<Relation> =
        getMapDataOrNull("node/$id/relations")?.relations.orEmpty()

    /**
     * Return all relations in which the given way is used.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getRelationsForWay(id: Long): Collection<Relation> =
        getMapDataOrNull("way/$id/relations")?.relations.orEmpty()

    /**
     * Return all relations in which the given relation is used.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getRelationsForRelation(id: Long): Collection<Relation> =
        getMapDataOrNull("relation/$id/relations")?.relations.orEmpty()

    private suspend fun getMapDataOrNull(query: String): MapData? = wrapApiClientExceptions {
        try {
            val response = httpClient.get(baseUrl + query) { expectSuccess = true }
            val source = response.bodyAsChannel().asSource().buffered()
            return parser.parseMapData(source) { false }
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.Gone, HttpStatusCode.NotFound -> return null
                else -> throw e
            }
        }
    }
}

/** While adding changes to our changeset, the API reports that the changeset limit is already
 *  reached. We must create a new changeset */
class ChangesetTooLargeException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause)
