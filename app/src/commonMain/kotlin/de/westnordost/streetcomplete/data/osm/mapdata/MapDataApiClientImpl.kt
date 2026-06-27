package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.ConflictException
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

class MapDataApiClientImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val userAccessTokenSource: UserAccessTokenSource,
    private val parser: MapDataApiParser,
    private val serializer: MapDataApiSerializer,
) : MapDataApiClient {

    override suspend fun uploadChanges(
        changesetId: Long,
        changes: MapDataChanges,
        ignoreRelation: (tags: Map<String, String>) -> Boolean
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

    override suspend fun getMap(
        bounds: BoundingBox,
        ignoreRelation: (tags: Map<String, String>) -> Boolean
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

    override suspend fun getWayComplete(id: Long): MapData? =
        getMapDataOrNull("way/$id/full")

    override suspend fun getRelationComplete(id: Long): MapData? =
        getMapDataOrNull("relation/$id/full")

    override suspend fun getNode(id: Long): Node? =
        getMapDataOrNull("node/$id")?.nodes?.single()

    override suspend fun getWay(id: Long): Way? =
        getMapDataOrNull("way/$id")?.ways?.single()

    override suspend fun getRelation(id: Long): Relation? =
        getMapDataOrNull("relation/$id")?.relations?.single()

    override suspend fun getWaysForNode(id: Long): Collection<Way> =
        getMapDataOrNull("node/$id/ways")?.ways.orEmpty()

    override suspend fun getRelationsForNode(id: Long): Collection<Relation> =
        getMapDataOrNull("node/$id/relations")?.relations.orEmpty()

    override suspend fun getRelationsForWay(id: Long): Collection<Relation> =
        getMapDataOrNull("way/$id/relations")?.relations.orEmpty()

    override suspend fun getRelationsForRelation(id: Long): Collection<Relation> =
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
