package de.westnordost.streetcomplete.data.osm.mapdata

import kotlinx.coroutines.runBlocking

/** Blocking wrapper around MapDataApiClient
 *
 *  TODO: In the long term, MapDataRepository itself should have a suspending interface, however
 *        this is a quite far-reaching refactor, as MapDataRepository is used by quite some
 *        blocking code. E.g. MapDataController and MapDataWithEditsSource should then also
 *        suspend. */
class RemoteMapDataRepository(private val mapDataApiClient: MapDataApiClient) : MapDataRepository {
    override fun getNode(id: Long) = runBlocking { mapDataApiClient.getNode(id) }
    override fun getWay(id: Long) = runBlocking { mapDataApiClient.getWay(id) }
    override fun getRelation(id: Long) = runBlocking { mapDataApiClient.getRelation(id) }
    override fun getWayComplete(id: Long) = runBlocking { mapDataApiClient.getWayComplete(id) }
    override fun getRelationComplete(id: Long) = runBlocking { mapDataApiClient.getRelationComplete(id) }
    override fun getWaysForNode(id: Long) = runBlocking { mapDataApiClient.getWaysForNode(id) }
    override fun getRelationsForNode(id: Long) = runBlocking { mapDataApiClient.getRelationsForNode(id) }
    override fun getRelationsForWay(id: Long) = runBlocking { mapDataApiClient.getRelationsForWay(id) }
    override fun getRelationsForRelation(id: Long) = runBlocking { mapDataApiClient.getRelationsForRelation(id) }
}
