package de.westnordost.streetcomplete.data

import de.westnordost.osmapi.common.errors.*
import de.westnordost.osmapi.map.ElementUpdates
import de.westnordost.osmapi.map.MapData
import de.westnordost.osmapi.map.handler.MapDataHandler
import de.westnordost.streetcomplete.data.osm.mapdata.*


/** Get and upload changes to map data */
interface MapDataApi {

    /**
     * Upload changes into an opened changeset.
     *
     * @param changesetId id of the changeset to upload changes into
     * @param elements elements to upload. No special order required
     *
     * @throws OsmNotFoundException if the changeset does not exist (yet) or an element in the
     *                              does not exist
     * @throws OsmConflictException if the changeset has already been closed, there is a conflict
     *                              for the elements being uploaded or the user who created the
     *                              changeset is not the same as the one uploading the change
     * @throws OsmAuthorizationException if the application does not have permission to edit the
     *                                   map (Permission.MODIFY_MAP)
     * @throws OsmPreconditionFailedException if the deletion of an element was uploaded but that
     *                                        element is still referred to by another element
     *
     * @return the updated elements
     */
    fun uploadChanges(changesetId: Long, elements: Collection<Element>): ElementUpdates

    /**
     * Open a new changeset with the given tags
     *
     * @param tags tags of this changeset. Usually it is comment and source.
     *
     * @throws OsmAuthorizationException if the application does not have permission to edit the
     *                                   map (Permission.MODIFY_MAP)
     * @return the id of the changeset
     */
    fun openChangeset(tags: Map<String, String>): Long

    /**
     * Closes the given changeset.
     *
     * @param changesetId id of the changeset to close
     *
     * @throws OsmConflictException if the changeset has already been closed
     * @throws OsmNotFoundException if the changeset does not exist (yet)
     * @throws OsmAuthorizationException if the application does not have permission to edit the
     *                                   map (Permission.MODIFY_MAP)
     */
    fun closeChangeset(changesetId: Long)

    /**
     * Feeds map data to the given MapDataHandler.
     * If not logged in, the Changeset for each returned element will be null
     *
     * @param bounds rectangle in which to query map data. May not cross the 180th meridian. This is
     * usually limited at 0.25 square degrees. Check the server capabilities.
     * @param handler map data handler that is fed the map data
     *
     * @throws OsmQueryTooBigException if the bounds are is too large
     * @throws IllegalArgumentException if the bounds cross the 180th meridian.
     *
     * @return the map data
     */
    fun getMap(bounds: BoundingBox, handler: MapDataHandler)

    /**
     * Queries the way with the given id plus all nodes that are in referenced by it.
     * If not logged in, the Changeset for each returned element will be null
     *
     * @param id the way's id
     *
     * @throws OsmNotFoundException if the way with the given id does not exist
     *
     * @return the map data
     */
    fun getWayComplete(id: Long): MapData?

    /**
     * Queries the relation with the given id plus all it's members and all nodes of ways that are
     * members of the relation.
     * If not logged in, the Changeset for each returned element will be null
     *
     * @param id the relation's id
     *
     * @throws OsmNotFoundException if the relation with the given id does not exist
     *
     * @return the map data
     */
    fun getRelationComplete(id: Long): MapData?

    /**
     * Note that if not logged in, the Changeset for each returned element will be null
     *
     * @param id the node's id
     *
     * @return the node with the given id or null if it does not exist
     */
    fun getNode(id: Long): Node?

    /**
     * Note that if not logged in, the Changeset for each returned element will be null
     *
     * @param id the way's id
     *
     * @return the way with the given id or null if it does not exist
     */
    fun getWay(id: Long): Way?

    /**
     * Note that if not logged in, the Changeset for each returned element will be null
     *
     * @param id the relation's id
     *
     * @return the relation with the given id or null if it does not exist
     */
    fun getRelation(id: Long): Relation?

    /**
     * Note that if not logged in, the Changeset for each returned element will be null
     *
     * @param id the node's id
     *
     * @return all ways that reference the node with the given id. Empty if none.
     */
    fun getWaysForNode(id: Long): List<Way>

    /**
     * Note that if not logged in, the Changeset for each returned element will be null
     *
     * @param id the node's id
     *
     * @return all relations that reference the node with the given id. Empty if none.
     */
    fun getRelationsForNode(id: Long): List<Relation>

    /**
     * Note that if not logged in, the Changeset for each returned element will be null
     *
     * @param id the way's id
     *
     * @return all relations that reference the way with the given id. Empty if none.
     */
    fun getRelationsForWay(id: Long): List<Relation>

    /**
     * Note that if not logged in, the Changeset for each returned element will be null
     *
     * @param id the relation's id
     *
     * @return all relations that reference the relation with the given id. Empty if none.
     */
    fun getRelationsForRelation(id: Long): List<Relation>
}
