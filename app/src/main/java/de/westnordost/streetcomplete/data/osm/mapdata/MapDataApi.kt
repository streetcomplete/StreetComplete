package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.download.QueryTooBigException

/** Get and upload changes to map data */
interface MapDataApi : MapDataRepository {

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
     *                                (Permission.MODIFY_MAP)
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the updated elements
     */
    fun uploadChanges(changesetId: Long, changes: MapDataChanges, ignoreRelationTypes: Set<String?> = emptySet()): MapDataUpdates

    /**
     * Open a new changeset with the given tags
     *
     * @param tags tags of this changeset. Usually it is comment and source.
     *
     * @throws AuthorizationException if the application does not have permission to edit the map
     *                                (Permission.MODIFY_MAP)
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the id of the changeset
     */
    fun openChangeset(tags: Map<String, String?>): Long

    /**
     * Closes the given changeset.
     *
     * @param changesetId id of the changeset to close
     *
     * @throws ConflictException if the changeset has already been closed
     * @throws AuthorizationException if the application does not have permission to edit the map
     *                                (Permission.MODIFY_MAP)
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    fun closeChangeset(changesetId: Long)

    /**
     * Feeds map data to the given MapDataHandler.
     * If not logged in, the Changeset for each returned element will be null
     *
     * @param bounds rectangle in which to query map data. May not cross the 180th meridian. This is
     * usually limited at 0.25 square degrees. Check the server capabilities.
     * @param mutableMapData mutable map data to add the add the data to
     * @param ignoreRelationTypes don't put any relations of the given types in the given mutableMapData
     *
     * @throws QueryTooBigException if the bounds area is too large
     * @throws IllegalArgumentException if the bounds cross the 180th meridian.
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the map data
     */
    fun getMap(bounds: BoundingBox, mutableMapData: MutableMapData, ignoreRelationTypes: Set<String?> = emptySet())

    /**
     * Queries the way with the given id plus all nodes that are in referenced by it.
     * If not logged in, the Changeset for each returned element will be null
     *
     * @param id the way's id
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the map data
     */
    override fun getWayComplete(id: Long): MapData?

    /**
     * Queries the relation with the given id plus all it's members and all nodes of ways that are
     * members of the relation.
     * If not logged in, the Changeset for each returned element will be null
     *
     * @param id the relation's id
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the map data
     */
    override fun getRelationComplete(id: Long): MapData?

    /**
     * Note that if not logged in, the Changeset for each returned element will be null
     *
     * @param id the node's id
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the node with the given id or null if it does not exist
     */
    override fun getNode(id: Long): Node?

    /**
     * Note that if not logged in, the Changeset for each returned element will be null
     *
     * @param id the way's id
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the way with the given id or null if it does not exist
     */
    override fun getWay(id: Long): Way?

    /**
     * Note that if not logged in, the Changeset for each returned element will be null
     *
     * @param id the relation's id
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the relation with the given id or null if it does not exist
     */
    override fun getRelation(id: Long): Relation?

    /**
     * Note that if not logged in, the Changeset for each returned element will be null
     *
     * @param id the node's id
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return all ways that reference the node with the given id. Empty if none.
     */
    override fun getWaysForNode(id: Long): List<Way>

    /**
     * Note that if not logged in, the Changeset for each returned element will be null
     *
     * @param id the node's id
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return all relations that reference the node with the given id. Empty if none.
     */
    override fun getRelationsForNode(id: Long): List<Relation>

    /**
     * Note that if not logged in, the Changeset for each returned element will be null
     *
     * @param id the way's id
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return all relations that reference the way with the given id. Empty if none.
     */
    override fun getRelationsForWay(id: Long): List<Relation>

    /**
     * Note that if not logged in, the Changeset for each returned element will be null
     *
     * @param id the relation's id
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return all relations that reference the relation with the given id. Empty if none.
     */
    override fun getRelationsForRelation(id: Long): List<Relation>
}

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
