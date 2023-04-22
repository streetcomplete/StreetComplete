package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.osm.edits.move.MoveNodeAction
import de.westnordost.streetcomplete.data.osm.edits.move.RevertMoveNodeAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.RELATION
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.WAY
import de.westnordost.streetcomplete.data.osm.mapdata.MapData
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapData
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.util.math.intersect
import java.util.concurrent.CopyOnWriteArrayList

/** Source for map data. It combines the original data downloaded with the edits made.
 *
 *  This class is threadsafe.
 * */
class MapDataWithEditsSource internal constructor(
    private val mapDataController: MapDataController,
    private val elementEditsController: ElementEditsController,
    private val elementGeometryCreator: ElementGeometryCreator
) : MapDataRepository {

    /** Interface to be notified of new or updated OSM elements */
    interface Listener {
        /** Called when a number of elements have been updated or deleted */
        fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>)

        /** Called when all elements in the given bounding box should be replaced with the elements
         *  in the mapDataWithGeometry */
        fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry)

        /** Called when all map data has been cleared */
        fun onCleared()
    }
    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()

    /* For thread-safety, all access to these three fields is synchronized. Since there is no hell
     * of parallelism, simply any method that somehow accesses these fields (~just about any method
     * in this class) is marked synchronized */
    private val deletedElements = HashSet<ElementKey>()
    private val updatedElements = HashMap<ElementKey, Element>()
    private val updatedGeometries = HashMap<ElementKey, ElementGeometry?>()

    private val mapDataListener = object : MapDataController.Listener {

        override fun onUpdated(updated: MutableMapDataWithGeometry, deleted: Collection<ElementKey>) {
            val modifiedElements = ArrayList<Pair<Element, ElementGeometry?>>()
            val modifiedDeleted = ArrayList<ElementKey>()
            synchronized(this) {
                /* We don't want to callOnUpdated if none of the changes affects map data provided
                 * by MapDataWithEditsSource.
                 * This is the case if
                 *  * All keys in deleted are already in deletedElements.
                 *  * The modified versions of all elements in updated are the same before and after
                 *    rebuildLocalChanges, except for the timestamp (expected to have few ms
                 *    difference) and version (never updated locally).
                 */
                val deletedIsUnchanged = deletedElements.containsAll(deleted)
                val elementsThatMightHaveChangedByKey = updated.mapNotNull { element ->
                    val key = element.key
                    if (element.isEqualExceptVersionAndTimestamp(updatedElements[key])) {
                        null // we already have the updated version, so this element is unchanged
                    } else {
                        key to get(element.type, element.id) // elementKey and element as provided by MapDataWithEditsSource
                    }
                }

                rebuildLocalChanges()

                /* nothingChanged can be false at this point when e.g. there are two edits on the
                   same element, and onUpdated is called after the first edit is uploaded. */
                val nothingChanged = deletedIsUnchanged && elementsThatMightHaveChangedByKey.all {
                    val updatedElement = get(it.first.type, it.first.id)
                    // old and new elements are equal except version and timestamp, or both are null
                    it.second?.isEqualExceptVersionAndTimestamp(updatedElement) ?: (updatedElement == null)
                }
                if (nothingChanged) {
                    return
                }

                for (element in updated) {
                    val key = element.key
                    // an element contained in the update that was deleted by an edit shall be deleted
                    if (deletedElements.contains(key)) {
                        modifiedDeleted.add(key)
                    }
                    // otherwise, update if it was modified at all
                    else {
                        val modifiedElement = updatedElements[key] ?: element
                        val modifiedGeometry = updatedGeometries[key] ?: updated.getGeometry(key.type, key.id)
                        modifiedElements.add(Pair(modifiedElement, modifiedGeometry))
                    }
                }

                for (key in deleted) {
                    val modifiedElement = updatedElements[key]
                    // en element that was deleted shall not be deleted but instead added to the updates if it was updated by an edit
                    if (modifiedElement != null) {
                        modifiedElements.add(Pair(modifiedElement, updatedGeometries[key]))
                    }
                    // otherwise, pass it through
                    else {
                        modifiedDeleted.add(key)
                    }
                }

                for ((element, geometry) in modifiedElements) {
                    updated.put(element, geometry)
                }
                for (key in modifiedDeleted) {
                    updated.remove(key.type, key.id)
                }
            }

            callOnUpdated(updated = updated, deleted = modifiedDeleted)
        }

        override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MutableMapDataWithGeometry) {
            synchronized(this) {
                rebuildLocalChanges()
                modifyBBoxMapData(bbox, mapDataWithGeometry)
            }

            callOnReplacedForBBox(bbox, mapDataWithGeometry)
        }

        override fun onCleared() {
            synchronized(this) {
                deletedElements.clear()
                updatedElements.clear()
                updatedGeometries.clear()
            }
            callOnCleared()
        }
    }

    private val elementEditsListener = object : ElementEditsSource.Listener {
        override fun onAddedEdit(edit: ElementEdit) {
            val mapData = MutableMapDataWithGeometry()
            val elementsToDelete: Collection<ElementKey>
            synchronized(this) {
                val mapDataUpdates = applyEdit(edit) ?: return
                elementsToDelete = mapDataUpdates.deleted
                for (element in mapDataUpdates.updated) {
                    mapData.put(element, getGeometry(element.type, element.id))
                }
            }

            callOnUpdated(updated = mapData, deleted = elementsToDelete)
        }

        override fun onSyncedEdit(edit: ElementEdit) {
            /* do nothing: If the change was synced successfully, it means that our local change
               was accepted by the server. There will also be a call to onUpdated
               in MapDataSource.Listener any moment now */
        }

        override fun onDeletedEdits(edits: List<ElementEdit>) {
            val mapData = MutableMapDataWithGeometry()
            val deletedElementKeys: MutableList<ElementKey>
            synchronized(this) {
                rebuildLocalChanges()

                deletedElementKeys = edits
                    .flatMap { elementEditsController.getIdProvider(it.id).getAll() }
                    .toMutableList()

                val editedElementKeys = edits.flatMap { it.action.elementKeys }.toSet()

                for (key in editedElementKeys) {
                    val element = get(key.type, key.id)
                    if (element != null) {
                        mapData.put(element, getGeometry(key.type, key.id))
                    } else {
                        // element that got edited by the deleted edit not found? Hmm, okay then (not sure if this can happen at all)
                        deletedElementKeys.add(key)
                    }
                }

                for (edit in edits) {
                    for (element in getElementsWithChangedGeometry(edit)) {
                        mapData.put(element, getGeometry(element.type, element.id))
                    }
                }
            }

            callOnUpdated(updated = mapData, deleted = deletedElementKeys)
        }
    }

    init {
        rebuildLocalChanges()
        mapDataController.addListener(mapDataListener)
        elementEditsController.addListener(elementEditsListener)
    }

    override fun get(type: ElementType, id: Long): Element? = synchronized(this) {
        val key = ElementKey(type, id)
        if (deletedElements.contains(key)) return null

        return updatedElements[key] ?: mapDataController.get(type, id)
    }

    fun getGeometry(type: ElementType, id: Long): ElementGeometry? = synchronized(this) {
        val key = ElementKey(type, id)
        if (deletedElements.contains(key)) return null

        return if (updatedGeometries.containsKey(key)) {
            updatedGeometries[key]
        } else {
            mapDataController.getGeometry(type, id)
        }
    }

    fun getGeometries(keys: Collection<ElementKey>): List<ElementGeometryEntry> = synchronized(this) {
        val originalKeys = keys.filter { !deletedElements.contains(it) && !updatedGeometries.containsKey(it) }
        val updatedGeometries = keys.mapNotNull { key ->
            updatedGeometries[key]?.let { ElementGeometryEntry(key.type, key.id, it) }
        }
        val originalGeometries = mapDataController.getGeometries(originalKeys)
        return updatedGeometries + originalGeometries
    }

    fun getMapDataWithGeometry(bbox: BoundingBox): MapDataWithGeometry = synchronized(this) {
        val mapDataWithGeometry = mapDataController.getMapDataWithGeometry(bbox)
        modifyBBoxMapData(bbox, mapDataWithGeometry)
        return mapDataWithGeometry
    }

    /* ----------------------------------- MapDataRepository ------------------------------------ */

    override fun getNode(id: Long): Node? = get(NODE, id) as? Node
    override fun getWay(id: Long): Way? = get(WAY, id) as? Way
    override fun getRelation(id: Long): Relation? = get(RELATION, id) as? Relation

    override fun getWayComplete(id: Long): MapData? = synchronized(this) {
        val way = getWay(id) ?: return null
        val mapData = getWayElements(way) ?: return null
        mapData.addAll(listOf(way))
        return mapData
    }

    private fun getWayElements(way: Way): MutableMapData? = synchronized(this) {
        val ids = way.nodeIds.toSet()
        val nodes = getNodes(ids)

        /* If the way is (now) not complete, this is not acceptable */
        if (nodes.size < ids.size) return null

        return MutableMapData(nodes)
    }

    private fun getNodes(ids: Set<Long>): Collection<Node> = synchronized(this) {
        val nodes = mapDataController.getNodes(ids)
        val nodesById = HashMap<Long, Node>()
        nodes.associateByTo(nodesById) { it.id }

        for (element in updatedElements.values) {
            if (element is Node) {
                // if a node is part of the way, put the updated node into the map
                if (ids.contains(element.id)) {
                    nodesById[element.id] = element
                }
            }
        }
        for (key in deletedElements) {
            if (key.type == NODE) {
                nodesById.remove(key.id)
            }
        }
        return nodesById.values
    }

    override fun getRelationComplete(id: Long): MapData? = synchronized(this) {
        val relation = getRelation(id) ?: return null
        val mapData = getRelationElements(relation)
        mapData.addAll(listOf(relation))
        return mapData
    }

    private fun getRelationElements(relation: Relation): MutableMapData = synchronized(this) {
        val elements = ArrayList<Element>()
        for (member in relation.members) {
            /* for way members, also get their nodes */
            if (member.type == WAY) {
                val wayComplete = getWayComplete(member.ref)
                if (wayComplete != null) {
                    elements.addAll(wayComplete)
                }
            } else {
                val element = get(member.type, member.ref)
                if (element != null) {
                    elements.add(element)
                }
            }
        }

        /* Even though the function name says "complete", it is acceptable for relations if after
         *  all, not all members are included */

        return MutableMapData(elements)
    }

    override fun getWaysForNode(id: Long): Collection<Way> = synchronized(this) {
        val waysById = HashMap<Long, Way>()
        mapDataController.getWaysForNode(id).associateByTo(waysById) { it.id }

        for (element in updatedElements.values) {
            if (element is Way) {
                // if the updated version of a way contains the node, put/replace the updated way
                if (element.nodeIds.contains(id)) {
                    waysById[element.id] = element
                }
                // if the updated version does not contain the node (anymore), we need to remove it
                // from the output set (=an edit removed that node) - if it was contained at all
                else {
                    waysById.remove(element.id)
                }
            }
        }
        for (key in deletedElements) {
            if (key.type == WAY) {
                waysById.remove(key.id)
            }
        }

        return waysById.values
    }

    override fun getRelationsForNode(id: Long): Collection<Relation> = getRelationsForElement(NODE, id)

    override fun getRelationsForWay(id: Long): Collection<Relation> = getRelationsForElement(WAY, id)

    override fun getRelationsForRelation(id: Long): Collection<Relation> = getRelationsForElement(RELATION, id)

    fun getRelationsForElement(type: ElementType, id: Long): Collection<Relation> = synchronized(this) {
        val relationsById = HashMap<Long, Relation>()
        val relations = when (type) {
            NODE -> mapDataController.getRelationsForNode(id)
            WAY -> mapDataController.getRelationsForWay(id)
            RELATION -> mapDataController.getRelationsForRelation(id)
        }
        relations.associateByTo(relationsById) { it.id }

        for (element in updatedElements.values) {
            if (element is Relation) {
                // if the updated version of a relation contains the node, put/replace the updated relation
                if (element.members.any { it.type == type && it.ref == id }) {
                    relationsById[element.id] = element
                }
                // if the updated version does not contain the node (anymore), we need to remove it
                // from the output set (=an edit removed that node) - if it was contained at all
                else {
                    relationsById.remove(element.id)
                }
            }
        }
        for (key in deletedElements) {
            if (key.type == RELATION) {
                relationsById.remove(key.id)
            }
        }

        return relationsById.values
    }

    /* ------------------------------------------------------------------------------------------ */

    private fun modifyBBoxMapData(bbox: BoundingBox, mapData: MutableMapDataWithGeometry) = synchronized(this) {
        for ((key, geometry) in updatedGeometries) {
            // add the modified data if it is in the bbox
            if (geometry != null && geometry.getBounds().intersect(bbox)) {
                val element = updatedElements[key]
                if (element != null) {
                    mapData.put(element, geometry)
                }
            }
            // or otherwise remove if it is not (anymore)
            else {
                mapData.remove(key.type, key.id)
            }
        }
        // and remove elements that have been deleted
        for (key in deletedElements) {
            mapData.remove(key.type, key.id)
        }
    }

    private fun rebuildLocalChanges() = synchronized(this) {
        deletedElements.clear()
        updatedElements.clear()
        updatedGeometries.clear()
        val edits = elementEditsController.getAllUnsynced()
        for (edit in edits) {
            applyEdit(edit)
        }
    }

    private fun applyEdit(edit: ElementEdit): MapDataUpdates? = synchronized(this) {
        val idProvider = elementEditsController.getIdProvider(edit.id)

        val mapDataChanges: MapDataChanges
        try {
            mapDataChanges = edit.action.createUpdates(this, idProvider)
        } catch (e: ConflictException) {
            return null
        }

        val deletedKeys = mapDataChanges.deletions.map { it.key }
        for (key in deletedKeys) {
            deletedElements.add(key)
            updatedElements.remove(key)
            updatedGeometries.remove(key)
        }
        /* sorting by element type: first nodes, then ways, then relations. This is important
           because the geometry of (new) nodes is necessary to create the geometry of ways etc
         */
        val updates = (
            mapDataChanges.creations +
            mapDataChanges.modifications +
            getElementsWithChangedGeometry(edit)
        ).sortedBy { it.type.ordinal }

        for (element in updates) {
            val key = element.key
            deletedElements.remove(key)
            updatedElements[key] = element
            updatedGeometries[key] = createGeometry(element)
        }

        return MapDataUpdates(updated = updates, deleted = deletedKeys)
    }

    private fun getElementsWithChangedGeometry(edit: ElementEdit): Sequence<Element> {
        val movedNode = when (edit.action) {
            is MoveNodeAction -> edit.action.originalNode
            is RevertMoveNodeAction -> edit.action.originalNode
            else -> return emptySequence()
        }
        return sequence {
            val waysContainingNode = getWaysForNode(movedNode.id)
            val relationsContainingNode = getRelationsForNode(movedNode.id)
            val relationsContainingWayContainingNode = waysContainingNode.flatMap { getRelationsForWay(it.id) }

            yieldAll(waysContainingNode)
            yieldAll(relationsContainingNode)
            yieldAll(relationsContainingWayContainingNode)
        }
    }

    private fun createGeometry(element: Element): ElementGeometry? {
        return when (element) {
            is Node -> {
                elementGeometryCreator.create(element)
            }
            is Way -> {
                val wayMapData = getWayElements(element) ?: return null
                elementGeometryCreator.create(element, wayMapData)
            }
            is Relation -> {
                val relationMapData = getRelationElements(element)
                elementGeometryCreator.create(element, relationMapData, true)
            }
        }
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun callOnUpdated(updated: MapDataWithGeometry = MutableMapDataWithGeometry(), deleted: Collection<ElementKey> = emptyList()) {
        if (updated.size == 0 && deleted.isEmpty()) return
        listeners.forEach { it.onUpdated(updated, deleted) }
    }
    private fun callOnReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
        if (mapDataWithGeometry.size == 0) return
        listeners.forEach { it.onReplacedForBBox(bbox, mapDataWithGeometry) }
    }
    private fun callOnCleared() {
        listeners.forEach { it.onCleared() }
    }
}

private fun Element.isEqualExceptVersionAndTimestamp(element: Element?): Boolean =
    id == element?.id && tags == element.tags && type == element.type && when (this) {
        is Node -> position == (element as Node).position
        is Way -> nodeIds == (element as Way).nodeIds
        is Relation -> members == (element as Relation).members
    }
