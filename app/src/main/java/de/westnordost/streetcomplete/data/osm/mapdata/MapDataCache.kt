package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.download.tiles.minTileRect
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.util.SpatialCache

/**
 * Cache for MapDataController using SpatialCache for nodes
 * caches data returned by db, except for nodes
 * for tiles in spatial cache all elements and geometries are cached,
 * way and relation data outside the cached tiles may be cached, but will be removed on any trim
 */
class MapDataCache(
    private val tileZoom: Int,
    val maxTiles: Int,
    initialCapacity: Int,
    private val fetchMapData: (BoundingBox) -> Pair<Collection<Element>, Collection<ElementGeometryEntry>>, // used if the tile is not contained
) {
    private val spatialCache = SpatialCache(
        tileZoom,
        maxTiles,
        initialCapacity,
        { emptyList() }, // data is fetched using fetchMapData and put using spatialCache.replaceAllInBBox
        Node::id, Node::position
    )
    // initial values obtained from a spot check:
    //  approximately 80% of all elements were found to be nodes
    //  approximately every second node is part of a way
    //  more than 90% of elements are not part of a relation
    private val wayRelationCache = HashMap<ElementKey, Element?>(initialCapacity / 6)
    private val wayRelationGeometryCache = HashMap<ElementKey, ElementGeometry?>(initialCapacity / 6)
    private val wayIdsByNodeIdCache = HashMap<Long, MutableList<Long>>(initialCapacity / 2)
    private val relationIdsByElementKeyCache = HashMap<ElementKey, MutableList<Long>>(initialCapacity / 10)

    fun update(
        deletedKeys: Collection<ElementKey> = emptyList(),
        addedOrUpdatedElements: Iterable<Element> = emptyList(),
        addedOrUpdatedGeometries: Iterable<ElementGeometryEntry> = emptyList(),
        bbox: BoundingBox? = null
    ) {
        if (bbox == null)
            spatialCache.update(
                updatedOrAdded = addedOrUpdatedElements.filterIsInstance<Node>(),
                deleted = deletedKeys.mapNotNull { if (it.type == ElementType.NODE) it.id else null }
            )
        else {
            // really need to remove things here? everything inside bbox is replaced anyway, so it
            // can only affect nodes outside the bbox
            // but since the mapdata actually comes from a padded bbox, this might actually be relevant
            if (deletedKeys.isNotEmpty()) spatialCache.update(deleted = deletedKeys.mapNotNull { if (it.type == ElementType.NODE) it.id else null })
            spatialCache.replaceAllInBBox(addedOrUpdatedElements.filterIsInstance<Node>(), bbox)
        }

        synchronized(this) {
            // first delete
            deletedKeys.forEach { key ->
                val oldElement = wayRelationCache.remove(key)
                wayRelationGeometryCache.remove(key)
                if (key.type == ElementType.NODE) wayIdsByNodeIdCache.remove(key.id)
                if (oldElement is Way)
                    oldElement.nodeIds.forEach { wayIdsByNodeIdCache[it]?.remove(key.id) }
                else if (oldElement is Relation)
                    oldElement.members.forEach { relationIdsByElementKeyCache[ElementKey(it.type, it.ref)]?.remove(key.id) }
                relationIdsByElementKeyCache.remove(key)
            }
            // then add
            addedOrUpdatedGeometries.forEach {
                if (it.elementType != ElementType.NODE)
                    wayRelationGeometryCache[ElementKey(it.elementType, it.elementId)] = it.geometry
            }

            val updatedWays = addedOrUpdatedElements.filterIsInstance<Way>()
            val updatedRelations = addedOrUpdatedElements.filterIsInstance<Relation>()

            // add ways to wayRelationCache
            // and to wayIdsByNodeIdCache if nodeId is in spatialCache, because if not then we
            // can't be sure to have all ways for that node, which may give wrong entries in wayIdsByNodeIdCache
            updatedWays.forEach { way ->
                val key = ElementKey(ElementType.WAY, way.id)
                wayRelationCache[key]?.let { oldWay ->
                    // remove old way from wayIdsByNodeIdCache
                    (oldWay as Way).nodeIds.forEach {
                        wayIdsByNodeIdCache[it]?.remove(way.id)
                    }
                }
                // update/add way
                wayRelationCache[key] = way

                way.nodeIds.forEach {
                    // add to wayIdsByNodeIdCache if node is in spatialCache
                    if (spatialCache.get(it) != null)
                        wayIdsByNodeIdCache.getOrPut(it) { ArrayList(2) }.add(way.id)
                    else
                    // But if we already have an entry for that nodeId (cached from getWaysForNode),
                    // we definitely need to add the updated way
                        wayIdsByNodeIdCache[it]?.add(way.id)
                }
            }

            if (updatedRelations.isEmpty())
                return

            // for adding relations to relationIdsByElementKeyCache we want the element to be
            // in spatialCache, or have a node / member in spatialCache (same reasoning as for ways)
            val wayIdsWithNodesInSpatialCache = wayRelationCache.values.mapNotNull { element ->
                if (element is Way && element.nodeIds.any { spatialCache.get(it) != null })
                    element.id
                else null
            }.toHashSet()

            val relationIdsWithElementsInSpatialCache = wayRelationCache.values.mapNotNull {
                if (it is Relation && it.members.any { member ->
                        (member.type == ElementType.NODE && spatialCache.get(member.ref) != null)
                            || (member.type == ElementType.WAY && member.ref in wayIdsWithNodesInSpatialCache)
                    })
                    it.id
                else null
            }.toHashSet()

            updatedRelations.forEach { relation ->
                val key = ElementKey(ElementType.RELATION, relation.id)
                wayRelationCache[key]?.let { oldRelation ->
                    // remove old relation from relationIdsByElementKeyCache
                    (oldRelation as Relation).members.forEach {
                        relationIdsByElementKeyCache[ElementKey(it.type, it.ref)]?.remove(relation.id)
                    }
                }
                // update/add relation
                wayRelationCache[key] = relation

                relation.members.forEach {
                    val memberKey = ElementKey(it.type, it.ref)
                    if ((it.type == ElementType.NODE && spatialCache.get(it.ref) != null)
                            || (it.ref in wayIdsWithNodesInSpatialCache && it.type == ElementType.WAY)
                            || (it.ref in relationIdsWithElementsInSpatialCache && it.type == ElementType.RELATION)) // todo: this is unpredictable... what do?
                        relationIdsByElementKeyCache.getOrPut(memberKey) { ArrayList(2) }.add(relation.id)
                    else
                    // But if we already have an entry for that elementKey (cached from getRelationsForElement),
                    // we definitely need to add the updated relation
                        relationIdsByElementKeyCache[memberKey]?.add(relation.id)
                }
            }
        }

    }

    fun getElement(type: ElementType, id: Long, fetch: (ElementType, Long) -> Element?): Element? {
        val element =
            if (type == ElementType.NODE) spatialCache.get(id)
            else synchronized(this) { wayRelationCache.getOrPutIfNotNull(ElementKey(type, id)) { fetch(type, id) } }
        return element ?: fetch(type, id)
    }

    fun getNode(id: Long): Node? = spatialCache.get(id)

    fun getGeometry(type: ElementType, id: Long, fetch: (ElementType, Long) -> ElementGeometry?): ElementGeometry? {
        val geometry = if (type == ElementType.NODE) spatialCache.get(id)
            ?.let { ElementPointGeometry(it.position) }
        else synchronized(this) { wayRelationGeometryCache.getOrPutIfNotNull(ElementKey(type, id)) { fetch(type, id) } }
        return geometry ?: fetch(type, id)
    }

    fun getElements(
        elementKeys: Collection<ElementKey>,
        fetch: (Collection<ElementKey>) -> List<Element>
    ): List<Element> = synchronized(this) {
        val elements = spatialCache.getAll(elementKeys.mapNotNull { if (it.type == ElementType.NODE) it.id else null } ) +
            elementKeys.mapNotNull { wayRelationCache[it] }
        return if (elementKeys.size == elements.size) elements
        else {
            val cachedElementKeys = elements.map { ElementKey(it.type, it.id) }
            val fetchedElements = fetch(elementKeys.filterNot { it in cachedElementKeys })
            fetchedElements.forEach {
                if (it.type != ElementType.NODE)
                    wayRelationCache[ElementKey(it.type, it.id)] = it
            }
            elements + fetchedElements
        }
    }

    fun getNodes(ids: Collection<Long>, fetch: (Collection<Long>) -> List<Node>): List<Node> {
        val nodes = spatialCache.getAll(ids)
        return if (ids.size == nodes.size) nodes
        else {
            val cachedNodeIds = nodes.map { it.id }
            nodes + fetch(ids.filterNot { it in cachedNodeIds })
        }
    }
    fun getWays(ids: Collection<Long>, fetch: (Collection<Long>) -> List<Way>): List<Way> =
        getElements(ids.map { ElementKey(ElementType.WAY, it) }) { keys -> fetch(keys.map { it.id }) }
            .filterIsInstance<Way>()
    fun getRelations(ids: Collection<Long>, fetch: (Collection<Long>) -> List<Relation>): List<Relation> =
        getElements(ids.map { ElementKey(ElementType.RELATION, it) }) { keys -> fetch(keys.map { it.id }) }
            .filterIsInstance<Relation>()

    fun getGeometries(
        keys: Collection<ElementKey>,
        fetch: (Collection<ElementKey>) -> List<ElementGeometryEntry>
    ): List<ElementGeometryEntry> = synchronized(this){
        val geometries = spatialCache.getAll(keys.mapNotNull { if (it.type == ElementType.NODE) it.id else null } )
            .map { it.toElementGeometryEntry() } +
            keys.mapNotNull { key ->
                wayRelationGeometryCache[key]?.let { ElementGeometryEntry(key.type, key.id, it) }
            }
        return if (keys.size == geometries.size) geometries
        else {
            val cachedKeys = geometries.map { ElementKey(it.elementType, it.elementId) }
            val fetchedGeometries = fetch(keys.filterNot { it in cachedKeys })
            fetchedGeometries.forEach {
                if (it.elementType != ElementType.NODE)
                    wayRelationGeometryCache[ElementKey(it.elementType, it.elementId)] = it.geometry
            }
            geometries + fetchedGeometries
        }
    }

    fun getWaysForNode(id: Long, fetch: (Long) -> List<Way>): List<Way> = synchronized(this) {
        val wayIds = wayIdsByNodeIdCache.getOrPut(id) {
            val ways = fetch(id)
            for (way in ways) { wayRelationCache[ElementKey(ElementType.WAY, way.id)] = way }
            ways.map { it.id }.toMutableList()
        }
        return wayIds.map { wayRelationCache[ElementKey(ElementType.WAY, it)] as Way }
    }

    fun getRelationsForNode(id: Long, fetch: (Long) -> List<Relation>) =
        getRelationsForElement(ElementType.NODE, id) { fetch(id) }
    fun getRelationsForWay(id: Long, fetch: (Long) -> List<Relation>) =
        getRelationsForElement(ElementType.WAY, id) { fetch(id) }
    fun getRelationsForRelation(id: Long, fetch: (Long) -> List<Relation>) =
        getRelationsForElement(ElementType.RELATION, id) { fetch(id) }

    private fun getRelationsForElement(type: ElementType, id: Long, fetch: () -> List<Relation>): List<Relation> = synchronized(this) {
        val relationIds = relationIdsByElementKeyCache.getOrPut(ElementKey(type, id)) {
            val relations = fetch()
            relations.forEach { wayRelationCache[ElementKey(ElementType.RELATION, it.id)] = it }
            relations.map { it.id }.toMutableList()
        }
        return relationIds.map { wayRelationCache[ElementKey(ElementType.RELATION, it)] as Relation }
    }

    fun getMapDataWithGeometry(bbox: BoundingBox): MutableMapDataWithGeometry {
        // fetch non-cached tiles and put to caches
        val requiredTiles = bbox.enclosingTilesRect(tileZoom).asTilePosSequence().toList()
        val cachedTiles = spatialCache.getTiles()
        val tilesToFetch = requiredTiles.filterNot { it in cachedTiles }

        val result = MutableMapDataWithGeometry()
        result.boundingBox = bbox
        val nodes: Collection<Node>
        if (tilesToFetch.isNotEmpty()) {
            // fetch needed data
            val fetchBBox = tilesToFetch.minTileRect()!!.asBoundingBox(tileZoom)
            val (elements, geometries) = fetchMapData(fetchBBox)

            // get nodes from spatial cache
            // this may not contain all nodes, but tiles that were cached initially might
            // get dropped when the caches are updated
            // duplicate fetch might be unnecessary often, but it's fast compared to fetching from db
            nodes = HashSet<Node>().apply { addAll(spatialCache.get(bbox)) }

            // put data to caches
            update(addedOrUpdatedElements = elements, addedOrUpdatedGeometries = geometries, bbox = fetchBBox)

            // return data if we need exactly what was just fetched
            if (fetchBBox == bbox) {
                result.putAll(elements, geometries + elements.filterIsInstance<Node>().map { it.toElementGeometryEntry() })
                return result
            }

            // get nodes again, should contains the newly added nodes this time
            // (but maybe not the old ones)
            nodes.addAll(spatialCache.get(bbox))
        } else {
            nodes = spatialCache.get(bbox)
        }

        // todo: in my "other" cache putting nodes+geometries is much faster... why?
        //  tried giving each node a fixed .geometry val -> difference NOT caused by creating geometries
        //  in "other" cache, for 1 z16 tile, entire getMapDataWithGeometry is twice as fast as this line
        //  ... but that doesn't make sense, as it's also simply putting all elements + geometries
        //   into MutableMapDataWithGeometry using put(element, geometry)
        nodes.forEach { result.put(it, ElementPointGeometry(it.position)) }

        synchronized(this) {
            val wayIds = HashSet<Long>(nodes.size / 5)
            val relationIds = HashSet<Long>(nodes.size / 10)
            nodes.forEach { node ->
                wayIdsByNodeIdCache[node.id]?.let { wayIds.addAll(it) }
                relationIdsByElementKeyCache[ElementKey(ElementType.NODE, node.id)]?.let { relationIds.addAll(it) }
            }
            wayIds.forEach { wayId ->
                val key = ElementKey(ElementType.WAY, wayId)
                result.put(wayRelationCache[key]!!, wayRelationGeometryCache[key])
                relationIdsByElementKeyCache[ElementKey(ElementType.WAY, wayId)]?.let { relationIds.addAll(it) }
            }
            relationIds.forEach {
                val key = ElementKey(ElementType.RELATION, it)
                result.put(wayRelationCache[key]!!, wayRelationGeometryCache[key])
                // todo: what about relations of relations?
            }
        }

        // finally trim if we fetched new data, and spatialCache is full
        // trim to 90%, so trim is (probably) not immediately called on next fetch
        if (spatialCache.size >= maxTiles && tilesToFetch.isNotEmpty())
            trim((maxTiles * 9) / 10)
        return result
    }

    fun clear() {
        synchronized(this) {
            wayIdsByNodeIdCache.clear()
            relationIdsByElementKeyCache.clear()
            wayRelationCache.clear()
            wayRelationGeometryCache.clear()
            spatialCache.clear()
        }
    }

    fun trim(size: Int) {
        spatialCache.trim(size)
        trimNonSpatialCaches()
    }

    private fun trimNonSpatialCaches() {
        synchronized(this) {
            // ways with at least one node in cache should not be removed
            val wayIdsWithNodesInSpatialCache = wayRelationCache.values.mapNotNull { element ->
                if (element is Way && element.nodeIds.any { spatialCache.get(it) != null })
                    element.id
                else null
            }.toHashSet()

            // relations with at least one element in cache should not be removed
            val relationIdsWithElementsInSpatialCache = wayRelationCache.values.mapNotNull {
                if (it is Relation && it.members.any { member ->
                        (member.type == ElementType.NODE && spatialCache.get(member.ref) != null)
                            || (member.type == ElementType.WAY && member.ref in wayIdsWithNodesInSpatialCache)
                    })
                    it.id
                else null
            }.toHashSet()

            wayRelationCache.keys.retainAll {
                if (it.type == ElementType.RELATION)
                    it.id in relationIdsWithElementsInSpatialCache
                else it.id in wayIdsWithNodesInSpatialCache
            }
            wayRelationGeometryCache.keys.retainAll {
                if (it.type == ElementType.RELATION)
                    it.id in relationIdsWithElementsInSpatialCache
                else it.id in wayIdsWithNodesInSpatialCache
            }

            // now clean up wayIdsByNodeIdCache and relationIdsByElementKeyCache
            wayIdsByNodeIdCache.keys.retainAll { spatialCache.get(it) != null }
            relationIdsByElementKeyCache.keys.retainAll {
                (it.type == ElementType.NODE && spatialCache.get(it.id) != null)
                    || (it.type == ElementType.WAY && it.id in wayIdsWithNodesInSpatialCache)
                // todo: what about relations of relations?
            }
        }
    }

    private fun <K,V> HashMap<K, V>.getOrPutIfNotNull(key: K, valueOrNull: () -> V?): V? {
        val v = get(key)
        if (v == null)
            valueOrNull()?.let {
                put(key, it)
                return it
            }
        return v
    }

    private fun Node.toElementGeometryEntry() =
        ElementGeometryEntry(type, id, ElementPointGeometry(position))
}
