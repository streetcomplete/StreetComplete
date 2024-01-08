package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.download.tiles.minTileRect
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.util.SpatialCache
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isCompletelyInside

/**
 * Cache for MapDataController that uses SpatialCache for nodes (i.e. geometry) and hash maps
 * for ways, relations and their geometry.
 *
 * The [initialCapacity] is the initial capacity for nodes, the initial capacities for the other
 * element types are derived from that because the ratio is usually similar.
 *
 * Way and relation data outside the cached tiles may be cached, but will be removed on any trim
 */
class MapDataCache(
    private val tileZoom: Int,
    val maxTiles: Int,
    initialCapacity: Int,
    private val fetchMapData: (BoundingBox) -> Pair<Collection<Element>, Collection<ElementGeometryEntry>>, // used if the tile is not contained
    private val fetchNodes: (Collection<Long>) -> Collection<Node>,
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
    private val nodesOutsideSpatialCache = HashMap<Long, Node>(initialCapacity)
    private val wayCache = HashMap<Long, Way>(initialCapacity / 6)
    private val relationCache = HashMap<Long, Relation>(initialCapacity / 10)
    private val wayGeometryCache = HashMap<Long, ElementGeometry>(initialCapacity / 6)
    private val relationGeometryCache = HashMap<Long, ElementGeometry>(initialCapacity / 10)
    private val wayIdsByNodeIdCache = HashMap<Long, MutableList<Long>>(initialCapacity / 2)
    private val relationIdsByElementKeyCache = HashMap<ElementKey, MutableList<Long>>(initialCapacity / 10)

    /**
     * Removes elements and geometries with keys in [deletedKeys] from cache and puts the
     * [updatedElements] and [updatedGeometries] into cache.
     * If a [bbox] is provided, all tiles that make up the bbox are added to spatialCache,
     * or cleared if they already exist.
     */
    fun update(
        deletedKeys: Collection<ElementKey> = emptyList(),
        updatedElements: Iterable<Element> = emptyList(),
        updatedGeometries: Iterable<ElementGeometryEntry> = emptyList(),
        bbox: BoundingBox? = null
    ) { synchronized(this) {
        val updatedNodes = updatedElements.filterIsInstance<Node>()
        val deletedNodeIds = deletedKeys.mapNotNull { if (it.type == ElementType.NODE) it.id else null }
        if (bbox == null) {
            // just update nodes if the containing tile
            spatialCache.update(updatedOrAdded = updatedNodes, deleted = deletedNodeIds)
        } else {
            // delete first, then put bbox and nodes to spatialCache (adds/clears tiles in bbox)
            spatialCache.update(deleted = deletedNodeIds)
            spatialCache.replaceAllInBBox(updatedNodes, bbox)
        }
        // remove all cached nodes that are now in spatialCache
        nodesOutsideSpatialCache.keys.removeAll { spatialCache.get(it) != null }

        // delete nodes, ways and relations
        for (key in deletedKeys) {
            when (key.type) {
                ElementType.NODE -> {
                    wayIdsByNodeIdCache.remove(key.id)
                    nodesOutsideSpatialCache.remove(key.id)
                }
                ElementType.WAY -> {
                    val deletedWayNodeIds = wayCache.remove(key.id)?.nodeIds.orEmpty()
                    for (nodeId in deletedWayNodeIds) {
                        wayIdsByNodeIdCache[nodeId]?.remove(key.id)
                    }
                    wayGeometryCache.remove(key.id)
                }
                ElementType.RELATION -> {
                    val deletedRelationMembers = relationCache.remove(key.id)?.members.orEmpty()
                    for (member in deletedRelationMembers) {
                        relationIdsByElementKeyCache[member.key]?.remove(key.id)
                    }
                    relationGeometryCache.remove(key.id)
                }
            }
        }

        // update way and relation geometries
        for (entry in updatedGeometries) {
            if (entry.elementType == ElementType.WAY) {
                wayGeometryCache[entry.elementId] = entry.geometry
            } else if (entry.elementType == ElementType.RELATION) {
                relationGeometryCache[entry.elementId] = entry.geometry
            }
        }

        // add nodes that are not in spatialCache to nodeCache
        updatedNodes.forEach { if (spatialCache.get(it.id) == null) nodesOutsideSpatialCache[it.id] = it }

        // update ways
        val updatedWays = updatedElements.filterIsInstance<Way>()
        for (way in updatedWays) {
            // updated way may have different node ids than old one, so those need to be removed first
            val oldWay = wayCache[way.id]
            if (oldWay != null) {
                for (oldNodeId in oldWay.nodeIds) {
                    wayIdsByNodeIdCache[oldNodeId]?.remove(way.id)
                }
            }
            wayCache[way.id] = way
            // ...and then the new node ids added
            for (nodeId in way.nodeIds) {
                // only if the node is already in spatial cache, the way ids it refers to must be known:
                // wayIdsByNodeIdCache is required for getMapDataWithGeometry(bbox), because a way is
                // inside the bbox if it contains a node inside the bbox.
                // But when adding a new entry to wayIdsByNodeIdCache, we must be sure to have ALL
                // way(Id)s for that node cached. This is only possible if the node is in spatialCache,
                // or if an entry for that node already exists (cached from getWaysForNode).
                // Otherwise the cache may return an incomplete list of ways in getWaysForNode,
                // instead of fetching the correct list.
                val wayIdsReferredByNode = if (spatialCache.get(nodeId) != null) {
                    wayIdsByNodeIdCache.getOrPut(nodeId) { ArrayList(2) }
                } else {
                    wayIdsByNodeIdCache[nodeId]
                }
                wayIdsReferredByNode?.add(way.id)
            }
        }

        // update relations
        val updatedRelations = updatedElements.filterIsInstance<Relation>()
        if (updatedRelations.isNotEmpty()) {
            // for adding relations to relationIdsByElementKeyCache we want the element to be
            // in spatialCache, or have a node / member in spatialCache (same reasoning as for ways)
            val (wayIds, relationIds) = determineWayAndRelationIdsWithElementsInSpatialCache()

            for (relation in updatedRelations) {
                // old relation may now have different members, so they need to be removed first
                val oldRelation = relationCache[relation.id]
                if (oldRelation != null) {
                    for (oldMember in oldRelation.members) {
                        relationIdsByElementKeyCache[oldMember.key]?.remove(relation.id)
                    }
                }
                relationCache[relation.id] = relation

                // ...and then the new members added
                for (member in relation.members) {
                    val memberKey = member.key
                    // only if the node member is already in the spatial cache or any node of a member
                    // is, the relation ids it refers to must be known:
                    // relationIdsByElementKeyCache is required for getMapDataWithGeometry(bbox),
                    // because a relation is inside the bbox if it contains a member inside the bbox,
                    // see comment above for wayIdsReferredByNode
                    val isInSpatialCache = when (member.type) {
                        ElementType.NODE -> spatialCache.get(member.ref) != null
                        ElementType.WAY -> member.ref in wayIds
                        ElementType.RELATION -> member.ref in relationIds
                    }
                    val relationIdsReferredByMember = if (isInSpatialCache) {
                        relationIdsByElementKeyCache.getOrPut(memberKey) { ArrayList(2) }
                    } else {
                        relationIdsByElementKeyCache[memberKey]
                    }
                    relationIdsReferredByMember?.add(relation.id)
                }
            }
        }
    } }

    /**
     * Gets the element with the given [type] and [id] from cache. If the element is not cached,
     * [fetch] is called, and the result is cached and then returned.
     */
    fun getElement(
        type: ElementType,
        id: Long,
        fetch: (ElementType, Long) -> Element?
    ): Element? = synchronized(this) {
        when (type) {
            ElementType.NODE -> spatialCache.get(id) ?: nodesOutsideSpatialCache.getOrPutIfNotNull(id) { fetch(type, id) as? Node }
            ElementType.WAY -> wayCache.getOrPutIfNotNull(id) { fetch(type, id) as? Way }
            ElementType.RELATION -> relationCache.getOrPutIfNotNull(id) { fetch(type, id) as? Relation }
        }
    }

    /**
     * Gets the geometry of the element with the given [type] and [id] from cache. If the geometry
     * is not cached, [fetch] is called, and the result is cached and then returned.
     */
    fun getGeometry(
        type: ElementType,
        id: Long,
        fetch: (ElementType, Long) -> ElementGeometry?
    ): ElementGeometry? = synchronized(this) {
        return when (type) {
            ElementType.NODE -> getCachedNode(id)?.let { ElementPointGeometry(it.position) } ?: fetch(type, id)
            ElementType.WAY -> wayGeometryCache.getOrPutIfNotNull(id) { fetch(type, id) }
            ElementType.RELATION -> relationGeometryCache.getOrPutIfNotNull(id) { fetch(type, id) }
        }
    }

    /**
     * Gets the elements with the given [keys] from cache. If any of the elements are not
     * cached, [fetch] is called for the missing elements. The fetched elements are cached and the
     * complete list is returned.
     * Note that the elements are returned in no particular order.
     */
    fun getElements(
        keys: Collection<ElementKey>,
        fetch: (Collection<ElementKey>) -> List<Element>
    ): List<Element> = synchronized(this) {
        val cachedElements = keys.mapNotNull { key ->
            when (key.type) {
                ElementType.NODE -> getCachedNode(key.id)
                ElementType.WAY -> wayCache[key.id]
                ElementType.RELATION -> relationCache[key.id]
            }
        }

        // exit early if everything is cached
        if (keys.size == cachedElements.size) return cachedElements

        // otherwise, fetch the rest & save to cache
        val cachedKeys = cachedElements.map { it.key }.toSet()
        val keysToFetch = keys.filterNot { it in cachedKeys }
        val fetchedElements = fetch(keysToFetch)
        for (element in fetchedElements) {
            when (element.type) {
                ElementType.NODE -> nodesOutsideSpatialCache[element.id] = element as Node
                ElementType.WAY -> wayCache[element.id] = element as Way
                ElementType.RELATION -> relationCache[element.id] = element as Relation
            }
        }
        return cachedElements + fetchedElements
    }

    /** Gets the nodes with the given [ids] from cache. If any of the nodes are not cached, [fetch]
     *  is called for the missing nodes. */
    fun getNodes(ids: Collection<Long>, fetch: (Collection<Long>) -> List<Node>): List<Node> = synchronized(this) {
        val cachedNodes = ids.mapNotNull { getCachedNode(it) }
        if (ids.size == cachedNodes.size) return cachedNodes

        // not all in cache: must fetch the rest from db
        val cachedNodeIds = cachedNodes.map { it.id }.toSet()
        val missingNodeIds = ids.filterNot { it in cachedNodeIds }
        val fetchedNodes = fetch(missingNodeIds)
        fetchedNodes.forEach { nodesOutsideSpatialCache[it.id] = it }
        return cachedNodes + fetchedNodes
    }

    /** Gets the ways with the given [ids] from cache. If any of the ways are not cached, [fetch]
     *  is called for the missing ways. The fetched ways are cached and the complete list is
     *  returned. */
    fun getWays(ids: Collection<Long>, fetch: (Collection<Long>) -> List<Way>): List<Way> {
        val wayKeys = ids.map { ElementKey(ElementType.WAY, it) }
        return getElements(wayKeys) { keys -> fetch(keys.map { it.id }) }.filterIsInstance<Way>()
    }

    /** Gets the relations with the given [ids] from cache. If any of the relations are not cached,
     *  [fetch] is called for the missing relations. The fetched relations are cached and the
     *  complete list is returned. */
    fun getRelations(ids: Collection<Long>, fetch: (Collection<Long>) -> List<Relation>): List<Relation> {
        val relationKeys = ids.map { ElementKey(ElementType.RELATION, it) }
        return getElements(relationKeys) { keys -> fetch(keys.map { it.id }) }.filterIsInstance<Relation>()
    }

    /**
     * Gets the geometries of the elements with the given [keys] from cache. If any of the
     * geometries are not cached, [fetch] is called for the missing geometries. The fetched
     * geometries are cached and the complete list is returned.
     * Note that the elements are returned in no particular order.
     */
    fun getGeometries(
        keys: Collection<ElementKey>,
        fetch: (Collection<ElementKey>) -> List<ElementGeometryEntry>
    ): List<ElementGeometryEntry> = synchronized(this) {
        // the implementation here is quite identical to the implementation in getElements, only
        // that geometries and not elements are returned and thus different caches are accessed
        val cachedEntries = keys.mapNotNull { key ->
            when (key.type) {
                ElementType.NODE -> getCachedNode(key.id)?.let { ElementPointGeometry(it.position) }
                ElementType.WAY -> wayGeometryCache[key.id]
                ElementType.RELATION -> relationGeometryCache[key.id]
            }?.let { ElementGeometryEntry(key.type, key.id, it) }
        }

        // exit early if everything is cached
        if (keys.size == cachedEntries.size) return cachedEntries

        // otherwise, fetch the rest & save to cache
        val cachedKeys = cachedEntries.map { it.key }.toSet()
        val keysToFetch = keys.filterNot { it in cachedKeys }
        val fetchedEntries = fetch(keysToFetch)
        for (entry in fetchedEntries) {
            when (entry.elementType) {
                ElementType.WAY -> wayGeometryCache[entry.elementId] = entry.geometry
                ElementType.RELATION -> relationGeometryCache[entry.elementId] = entry.geometry
                else -> Unit
            }
        }
        return cachedEntries + fetchedEntries
    }

    /**
     * Gets all ways for the node with the given [id] from cache. If the list of ways is not known,
     * or any way is missing in cache, [fetch] is called and the result cached.
     */
    fun getWaysForNode(id: Long, fetch: (Long) -> List<Way>): List<Way> = synchronized(this) {
        val wayIds = wayIdsByNodeIdCache.getOrPut(id) {
            val ways = fetch(id)
            for (way in ways) { wayCache[way.id] = way }
            ways.map { it.id }.toMutableList()
        }
        return wayIds.mapNotNull { wayCache[it] }
    }

    /**
     * Gets all relations for the node with the given [id] from cache. If the list of relations is
     * not known, or any relation is missing in cache, [fetch] is called and the result cached.
     */
    fun getRelationsForNode(id: Long, fetch: (Long) -> List<Relation>) =
        getRelationsForElement(ElementType.NODE, id) { fetch(id) }

    /**
     * Gets all relations for way with the given [id] from cache. If the list of relations is not
     * known, or any relation is missing in cache, [fetch] is called and the result cached.
     */
    fun getRelationsForWay(id: Long, fetch: (Long) -> List<Relation>) =
        getRelationsForElement(ElementType.WAY, id) { fetch(id) }

    /**
     * Gets all relations for way with the given [id] from cache. If the list of relations is not
     * known, or any relation is missing in cache, [fetch] is called and the result cached.
     */
    fun getRelationsForRelation(id: Long, fetch: (Long) -> List<Relation>) =
        getRelationsForElement(ElementType.RELATION, id) { fetch(id) }

    private fun getRelationsForElement(
        type: ElementType,
        id: Long,
        fetch: () -> List<Relation>
    ): List<Relation> = synchronized(this) {
        val relationIds = relationIdsByElementKeyCache.getOrPut(ElementKey(type, id)) {
            val relations = fetch()
            for (relation in relations) { relationCache[relation.id] = relation }
            relations.map { it.id }.toMutableList()
        }
        return relationIds.mapNotNull { relationCache[it] }
    }

    /**
     * Gets all elements and geometries inside [bbox]. This returns all nodes, all ways containing
     * at least one of the nodes, and all relations containing at least one of the ways or nodes,
     * and their geometries.
     * If data is not cached, tiles containing the [bbox] are fetched from database and cached.
     */
    fun getMapDataWithGeometry(bbox: BoundingBox): MutableMapDataWithGeometry = synchronized(this) {
        val requiredTiles = bbox.enclosingTilesRect(tileZoom).asTilePosSequence().toList()
        val cachedTiles = spatialCache.getTiles()
        val tilesToFetch = requiredTiles.filterNot { it in cachedTiles }
        val tilesRectToFetch = tilesToFetch.minTileRect()

        val result = MutableMapDataWithGeometry()
        result.boundingBox = bbox
        val nodes: Collection<Node>
        if (tilesRectToFetch != null) {
            // fetch needed data
            val fetchBBox = tilesRectToFetch.asBoundingBox(tileZoom)
            val (elements, geometries) = fetchMapData(fetchBBox)

            // get nodes from spatial cache
            // this may not contain all nodes, but tiles that were cached initially might
            // get dropped when the caches are updated
            // duplicate fetch might be unnecessary in many cases, but it's very fast anyway

            // get(bbox) for tiles not in spatialCache calls spatialCache.fetch, but this is still
            // safe as tiles are replaced and properly filled as part of the following update
            nodes = HashSet<Node>(spatialCache.get(bbox))
            update(updatedElements = elements, updatedGeometries = geometries, bbox = fetchBBox)

            // return data if we need exactly what was just fetched
            if (fetchBBox == bbox) {
                val nodeGeometryEntries = elements.filterIsInstance<Node>().map { it.toElementGeometryEntry() }
                result.putAll(elements, geometries + nodeGeometryEntries)
                return result
            }

            // add newly fetched nodes from elements
            // getting nodes from spatialCache can cause issues, as tiles in the bbox may now be removed unexpectedly
            // see https://github.com/streetcomplete/StreetComplete/issues/4980#issuecomment-1531960544
            for (element in elements) {
                if (element !is Node) continue
                if (element.position in bbox) nodes.add(element)
            }
        } else {
            nodes = spatialCache.get(bbox)
        }

        val wayIds = HashSet<Long>(nodes.size / 5)
        val relationIds = HashSet<Long>(nodes.size / 10)
        for (node in nodes) {
            wayIdsByNodeIdCache[node.id]?.let { wayIds.addAll(it) }
            relationIdsByElementKeyCache[node.key]?.let { relationIds.addAll(it) }
            result.put(node, ElementPointGeometry(node.position))
        }

        val nodesToFetch = hashSetOf<Long>()
        for (wayId in wayIds) {
            val way = wayCache[wayId]!!
            val wayGeometry = wayGeometryCache[wayId]
            result.put(way, wayGeometry)
            relationIdsByElementKeyCache[way.key]?.let { relationIds.addAll(it) }

            // find all nodes that are part of the way, but not in result
            if (wayGeometry?.getBounds()?.isCompletelyInside(bbox) == true) continue // no need to check
            for (nodeId in way.nodeIds) {
                if (result.getNode(nodeId) != null) continue
                val cachedNode = getCachedNode(nodeId)
                if (cachedNode != null) {
                    result.put(cachedNode, ElementPointGeometry(cachedNode.position))
                    continue
                }
                nodesToFetch.add(nodeId)
            }
        }
        if (nodesToFetch.isNotEmpty()) {
            fetchNodes(nodesToFetch).forEach {
                nodesOutsideSpatialCache[it.id] = it
                result.put(it, ElementPointGeometry(it.position))
            }
        }

        for (relationId in relationIds) {
            result.put(relationCache[relationId]!!, relationGeometryCache[relationId])
            // don't add relations of relations, because elementDao.getAll(bbox) also isn't doing that
        }

        // trim if we fetched new data, and spatialCache is full
        // trim to 66%, so trim is (probably) not immediately called on next fetch
        if (spatialCache.size >= maxTiles && tilesToFetch.isNotEmpty()) {
            trim((maxTiles * 2) / 3)
        }
        return result
    }

    /** Clears the cache */
    fun clear() { synchronized(this) {
        spatialCache.clear()
        nodesOutsideSpatialCache.clear()
        wayCache.clear()
        relationCache.clear()
        wayGeometryCache.clear()
        relationGeometryCache.clear()
        wayIdsByNodeIdCache.clear()
        relationIdsByElementKeyCache.clear()
    } }

    /** Reduces cache size to the given number of non-empty [tiles], and removes all data
     *  not contained in the remaining tiles.
     */
    fun trim(tiles: Int) { synchronized(this) {
        spatialCache.trim(tiles)
        nodesOutsideSpatialCache.clear() // simply clear nodeCache, as transferring some nodes from spatialCache is slow

        // ways and relations with at least one element in cache should not be removed
        val (wayIds, relationIds) = determineWayAndRelationIdsWithElementsInSpatialCache()

        wayCache.keys.retainAll { it in wayIds }
        relationCache.keys.retainAll { it in relationIds }
        wayGeometryCache.keys.retainAll { it in wayIds }
        relationGeometryCache.keys.retainAll { it in relationIds }

        // now clean up wayIdsByNodeIdCache and relationIdsByElementKeyCache
        wayIdsByNodeIdCache.keys.retainAll { spatialCache.get(it) != null }
        relationIdsByElementKeyCache.keys.retainAll {
            when (it.type) {
                ElementType.NODE -> spatialCache.get(it.id) != null
                ElementType.WAY -> it.id in wayIds
                ElementType.RELATION -> it.id in relationIds
            }
        }
    } }

    /** return the ids of all ways whose nodes are in the spatial cache plus as all ids of
     *  relations referred to by those ways or nodes that are in the spatial cache */
    private fun determineWayAndRelationIdsWithElementsInSpatialCache(): Pair<Set<Long>, Set<Long>> {
        // note: wayIdsByNodeIdCache and relationIdsByElementKeyCache cannot be used here to get the
        // result because this method is called in places where the spatial cache has been updated
        // and now the other caches are outdated. So this method exists to find those elements that
        // are STILL referred to directly or indirectly by the spatial cache.

        val wayIds = HashSet<Long>(wayCache.size)
        for (way in wayCache.values) {
            if (way.nodeIds.any { spatialCache.get(it) != null }) {
                wayIds.add(way.id)
            }
        }

        fun RelationMember.isCachedWayOrNode(): Boolean =
            type == ElementType.NODE && spatialCache.get(ref) != null
                || type == ElementType.WAY && ref in wayIds

        fun RelationMember.hasCachedMembers(): Boolean =
            type == ElementType.RELATION
                && relationCache[ref]?.members?.any { it.isCachedWayOrNode() } == true

        val relationIds = HashSet<Long>(relationCache.size)
        for (relation in relationCache.values) {
            if (relation.members.any { it.isCachedWayOrNode() || it.hasCachedMembers() }) {
                relationIds.add(relation.id)
            }
        }
        return wayIds to relationIds
    }

    private fun <K, V> HashMap<K, V>.getOrPutIfNotNull(key: K, valueOrNull: () -> V?): V? {
        val v = get(key)
        if (v != null) return v

        val computed = valueOrNull()
        if (computed != null) put(key, computed)
        return computed
    }

    private fun Node.toElementGeometryEntry() =
        ElementGeometryEntry(type, id, ElementPointGeometry(position))

    private fun getCachedNode(id: Long): Node? = spatialCache.get(id) ?: nodesOutsideSpatialCache[id]
}
