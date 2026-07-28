package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.QueryTooBigException

/** Get and upload changes to map data */
interface MapDataApiClient {
    /**
     * Upload changes into an opened changeset.
     *
     * @param changesetId id of the changeset to upload changes into
     * @param changes changes to upload.
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
    ): MapDataUpdates

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
    ): MutableMapData

    /**
     * Returns the given way by id plus all its nodes or null if the way does not exist.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getWayComplete(id: Long): MapData?

    /**
     * Returns the given relation by id plus all its members and all nodes of ways that are members
     * of the relation. Or null if the relation does not exist.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getRelationComplete(id: Long): MapData?

    /**
     * Return the given node by id or null if it doesn't exist
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getNode(id: Long): Node?

    /**
     * Return the given way by id or null if it doesn't exist
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getWay(id: Long): Way?

    /**
     * Return the given relation by id or null if it doesn't exist
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getRelation(id: Long): Relation?

    /**
     * Return all ways in which the given node is used.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getWaysForNode(id: Long): Collection<Way>

    /**
     * Return all relations in which the given node is used.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getRelationsForNode(id: Long): Collection<Relation>

    /**
     * Return all relations in which the given way is used.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getRelationsForWay(id: Long): Collection<Relation>

    /**
     * Return all relations in which the given relation is used.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun getRelationsForRelation(id: Long): Collection<Relation>
}
