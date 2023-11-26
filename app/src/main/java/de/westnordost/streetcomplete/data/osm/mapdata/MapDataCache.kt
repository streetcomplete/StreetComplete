package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.download.tiles.upToTwoMinTileRects
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.util.SpatialCache
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isCompletelyInside
import kotlin.math.min

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
    private val maxTiles: Int,
    initialCapacity: Int,
    private val fetchMapData: (BoundingBox) -> Pair<Collection<Element>, Collection<ElementGeometryEntry>>, // used if the tile is not contained
    private val fetchNodes: (Collection<Long>) -> Collection<Node>,
) {
    // contains tiles that should not be trimmed, and number of calls to not trim this tile
    private val noTrim = HashMap<TilePos, Int>()

    private val spatialCache = SpatialCache(
        tileZoom,
        maxTiles,
        initialCapacity,
        { emptyList() }, // data is fetched using fetchMapData and put using spatialCache.replaceAllInBBox
        Node::key, Node::position,
        noTrim // actually noTrim.keys is sufficient, but whatever
    )
    // initial values obtained from a spot check:
    //  approximately 80% of all elements were found to be nodes
    //  approximately every second node is part of a way
    //  more than 90% of elements are not part of a relation
    private val notSpatialCache = HashMap<ElementKey, Element>(initialCapacity / 4)
    private val wayRelationGeometryCache = HashMap<ElementKey, ElementGeometry>(initialCapacity / 5)
    private val wayKeyByNodeKeyCache = HashMap<ElementKey, MutableList<ElementKey>>(initialCapacity / 2)
    private val relationKeysByElementKeyCache = HashMap<ElementKey, MutableList<ElementKey>>(initialCapacity / 10)

    fun noTrimPlus(bbox: BoundingBox) = synchronized(this) {
        // bbox to tiles, and increase all by 1
        bbox.enclosingTilesRect(tileZoom).asTilePosSequence().forEach { tile ->
            noTrim[tile] = (noTrim[tile] ?: 0) + 1
        }
    }

    fun noTrimMinus(bbox: BoundingBox) = synchronized(this) {
        // if number is 0, remove tile from map
        bbox.enclosingTilesRect(tileZoom).asTilePosSequence().forEach { tile ->
            val newValue = (noTrim[tile] ?: 0) - 1
            if (newValue > 0) noTrim[tile] = newValue
            else noTrim.remove(tile)
        }
        if (spatialCache.size > maxTiles)
            trim(maxTiles * 2 / 3)
    }

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
        val deletedNodeKeys = deletedKeys.filter { it.type == ElementType.NODE }
        if (bbox == null) {
            // just update nodes if the containing tile
            spatialCache.update(updatedOrAdded = updatedNodes, deleted = deletedNodeKeys)
        } else {
            // delete first, then put bbox and nodes to spatialCache (adds/clears tiles in bbox)
            spatialCache.update(deleted = deletedNodeKeys)
            spatialCache.replaceAllInBBox(updatedNodes, bbox)
        }

        // delete nodes, ways and relations
        for (key in deletedKeys) {
            when (key.type) {
                ElementType.NODE -> {
                    wayKeyByNodeKeyCache.remove(key)
                    notSpatialCache.remove(key)
                }
                ElementType.WAY -> {
                    val deletedWayNodeIds = (notSpatialCache.remove(key) as? Way)?.nodeIds.orEmpty()
                    for (nodeId in deletedWayNodeIds) {
                        wayKeyByNodeKeyCache[ElementKey(ElementType.NODE, nodeId)]?.remove(key)
                    }
                    wayRelationGeometryCache.remove(key)
                }
                ElementType.RELATION -> {
                    val deletedRelationMembers = (notSpatialCache.remove(key) as? Relation)?.members.orEmpty()
                    for (member in deletedRelationMembers) {
                        relationKeysByElementKeyCache[member.key]?.remove(key)
                    }
                    wayRelationGeometryCache.remove(key)
                }
            }
        }

        // update way and relation geometries
        for (entry in updatedGeometries) {
            if (entry.elementType != ElementType.NODE)
                wayRelationGeometryCache[entry.reuseKey] = entry.geometry
        }

        if (bbox == null)
            updatedNodes.forEach {
                // updated nodes are either in spatialCache, then remove from notSpatialCache
                // or the are not, then add to notSpatialCache
                if (spatialCache.get(it.key) == null) notSpatialCache[it.key] = it
                else notSpatialCache.remove(it.key)
            }
        else {
            // spatialCache may have changed size, better remove all nodes that are not in spatialCache

            // remove all cached nodes that are now in spatialCache
            notSpatialCache.keys.removeAll { it.type == ElementType.NODE && spatialCache.get(it) != null }

            // add nodes that are not in spatialCache to nodeCache
            updatedNodes.forEach { if (spatialCache.get(it.key) == null) notSpatialCache[it.key] = it }
        }

        // update ways
        val updatedWays = updatedElements.filterIsInstance<Way>()
        for (way in updatedWays) {
            // updated way may have different node ids than old one, so those need to be removed first
            val oldWay = notSpatialCache[way.key] as? Way
            if (oldWay != null) {
                for (oldNodeId in oldWay.nodeIds) {
                    wayKeyByNodeKeyCache[ElementKey(ElementType.NODE, oldNodeId)]?.remove(way.key)
                }
            }
            notSpatialCache[way.key] = way
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
                val nodeKey = ElementKey(ElementType.NODE, nodeId)
                val node = spatialCache.get(nodeKey)
                val wayIdsReferredByNode = if (node != null) {
                    wayKeyByNodeKeyCache.getOrPut(node.key) { ArrayList(2) }
                } else {
                    wayKeyByNodeKeyCache[nodeKey]
                }
                wayIdsReferredByNode?.add(way.key)
            }
        }

        // update relations
        val updatedRelations = updatedElements.filterIsInstance<Relation>()
        if (updatedRelations.isNotEmpty()) {

            // for adding relations to relationIdsByElementKeyCache we want the element to be
            // in spatialCache, or have a node / member in spatialCache (same reasoning as for ways)
            val (wayIds, relationIds) = determineWayAndRelationIdsWithElementsInSpatialCache()

            lateinit var memberKey: ElementKey
            for (relation in updatedRelations) {
                // old relation may now have different members, so they need to be removed first
                val oldRelation = notSpatialCache[relation.key] as? Relation
                if (oldRelation != null) {
                    for (oldMember in oldRelation.members) {
                        relationKeysByElementKeyCache[oldMember.key]?.remove(relation.key)
                    }
                }
                notSpatialCache[relation.key] = relation
                // ...and then the new members added
                for (member in relation.members) {
                    memberKey = member.key
                    // only if the node member is already in the spatial cache or any node of a member
                    // is, the relation ids it refers to must be known:
                    // relationIdsByElementKeyCache is required for getMapDataWithGeometry(bbox),
                    // because a relation is inside the bbox if it contains a member inside the bbox,
                    // see comment above for wayIdsReferredByNode
                    val isInSpatialCache = when (member.type) {
                        ElementType.NODE -> {
                            val node = spatialCache.get(memberKey)
                            if (node != null) {
                                memberKey = node.key
                                true
                            } else false
                        }
                        ElementType.WAY -> {
                            if (member.ref in wayIds) {
                                notSpatialCache[memberKey]?.let { memberKey = it.key }
                                true
                            } else false
                        }
                        ElementType.RELATION -> member.ref in relationIds
                    }
                    val relationIdsReferredByMember = if (isInSpatialCache) {
                        relationKeysByElementKeyCache.getOrPut(memberKey) { ArrayList(2) }
                    } else {
                        relationKeysByElementKeyCache[memberKey]
                    }
                    relationIdsReferredByMember?.add(relation.key)
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
        val key = ElementKey(type, id)
        when (key.type) {
            ElementType.NODE -> spatialCache.get(key) ?: notSpatialCache.getOrPutIfNotNull(key) { fetch(key.type, key.id) }
            else -> notSpatialCache.getOrPutIfNotNull(key) { fetch(key.type, key.id) }
        }
    }

    /**
     * Gets the geometry of the element with the given [type] and [id] from cache. If the geometry
     * is not cached, [fetch] is called, and the result is cached and then returned.
     */
    fun getGeometry(
        type: ElementType,
        id: Long,
        fetch: (ElementType, Long) -> ElementGeometry?,
        fetchNode: (Long) -> Node?
    ): ElementGeometry? = synchronized(this) {
        val key = ElementKey(type, id)
        when (type) {
            ElementType.NODE -> (spatialCache.get(key) ?: notSpatialCache.getOrPutIfNotNull(key) { fetchNode(id) })?.let { ElementPointGeometry((it as Node).position) }
            else -> wayRelationGeometryCache.getOrPutIfNotNull(key) { fetch(type, id) }
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
                ElementType.NODE -> getCachedNode(key)
                else -> notSpatialCache[key]
            }
        }

        // exit early if everything is cached
        if (keys.size == cachedElements.size) return cachedElements

        // otherwise, fetch the rest & save to cache
        val cachedKeys = cachedElements.mapTo(HashSet(cachedElements.size)) { it.key }
        val keysToFetch = keys.filterNot { it in cachedKeys }
        val fetchedElements = fetch(keysToFetch)
        for (element in fetchedElements) {
            notSpatialCache[element.key] = element
        }
        return cachedElements + fetchedElements
    }

    /** Gets the nodes with the given [ids] from cache. If any of the nodes are not cached, [fetch]
     *  is called for the missing nodes. */
    fun getNodes(ids: Collection<Long>, fetch: (Collection<Long>) -> List<Node>): List<Node> = synchronized(this) {
        val cachedNodes = ids.mapNotNull { getCachedNode(ElementKey(ElementType.NODE, it)) }
        if (ids.size == cachedNodes.size) return cachedNodes

        // not all in cache: must fetch the rest from db
        val cachedNodeIds = cachedNodes.mapTo(HashSet(cachedNodes.size)) { it.id }
        val missingNodeIds = ids.filterNot { it in cachedNodeIds }
        val fetchedNodes = fetch(missingNodeIds)
        fetchedNodes.forEach { notSpatialCache[it.key] = it }
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
        fetch: (Collection<ElementKey>) -> List<ElementGeometryEntry>,
        fetchNodes: (Collection<Long>) -> List<Node>
    ): List<ElementGeometryEntry> = synchronized(this) {
        // the implementation here is quite identical to the implementation in getElements, only
        // that geometries and not elements are returned and thus different caches are accessed
        val cachedEntries = keys.mapNotNull { key ->
            when (key.type) {
                ElementType.NODE -> getCachedNode(key)?.let { ElementPointGeometry(it.position) }
                else -> wayRelationGeometryCache[key]
            }?.let { ElementGeometryEntry(key.type, key.id, it) }
        }

        // exit early if everything is cached
        if (keys.size == cachedEntries.size) return cachedEntries

        // otherwise, fetch the rest & save to cache
        val cachedKeys = cachedEntries.mapTo(HashSet(cachedEntries.size)) { it.key }
        val keysToFetch = keys.filterNot { it in cachedKeys }
        val fetchedEntries = fetch(keysToFetch.filterNot { it.type == ElementType.NODE }) // only fetch non-nodes
        for (entry in fetchedEntries) {
             wayRelationGeometryCache[entry.key] = entry.geometry // no nodes fetched anyway
        }
        // now fetch the nodes separately and add them to nodeCache
        val nodes = fetchNodes(keysToFetch.mapNotNull { if (it.type == ElementType.NODE) it.id else null })
        nodes.forEach { notSpatialCache[it.key] = it }
        return cachedEntries + fetchedEntries + nodes.map { it.toElementGeometryEntry() }
    }

    /**
     * Gets all ways for the node with the given [id] from cache. If the list of ways is not known,
     * or any way is missing in cache, [fetch] is called and the result cached.
     */
    fun getWaysForNode(id: Long, fetch: (Long) -> List<Way>): List<Way> = synchronized(this) {
        val wayIds = wayKeyByNodeKeyCache.getOrPut(ElementKey(ElementType.NODE, id)) {
            val ways = fetch(id)
            for (way in ways) { notSpatialCache[way.key] = way }
            ways.map { it.key }.toMutableList()
        }
        return wayIds.mapNotNull { notSpatialCache[it] as? Way }
    }

    /**
     * Gets all relations for the node with the given [id] from cache. If the list of relations is
     * not known, or any relation is missing in cache, [fetch] is called and the result cached.
     */
    fun getRelationsForNode(id: Long, fetch: (Long) -> List<Relation>) =
        getRelationsForElement(ElementKey(ElementType.NODE, id)) { fetch(id) }

    /**
     * Gets all relations for way with the given [id] from cache. If the list of relations is not
     * known, or any relation is missing in cache, [fetch] is called and the result cached.
     */
    fun getRelationsForWay(id: Long, fetch: (Long) -> List<Relation>) =
        getRelationsForElement(ElementKey(ElementType.WAY, id)) { fetch(id) }

    /**
     * Gets all relations for way with the given [id] from cache. If the list of relations is not
     * known, or any relation is missing in cache, [fetch] is called and the result cached.
     */
    fun getRelationsForRelation(id: Long, fetch: (Long) -> List<Relation>) =
        getRelationsForElement(ElementKey(ElementType.RELATION, id)) { fetch(id) }

    private fun getRelationsForElement(
        key: ElementKey,
        fetch: () -> List<Relation>
    ): List<Relation> = synchronized(this) {
        val relationIds = relationKeysByElementKeyCache.getOrPut(key) {
            val relations = fetch()
            for (relation in relations) { notSpatialCache[relation.key] = relation }
            relations.map { it.key }.toMutableList()
        }
        return relationIds.mapNotNull { notSpatialCache[it] as? Relation }
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
        val tilesRectsToFetch = requiredTiles.filterNot { it in cachedTiles }.upToTwoMinTileRects()

        val result: MutableMapDataWithGeometry
        if (tilesRectsToFetch != null) {
            Log.i(TAG, "need to fetch data in $tilesRectsToFetch from database")
            // get nodes from spatial cache
            // this may not contain all nodes, but tiles that were cached initially might
            // get dropped when the caches are updated
            // duplicate fetch might be unnecessary in many cases, but it's very fast anyway

            // get(bbox) for tiles not in spatialCache calls spatialCache.fetch, but this is still
            // safe as tiles are replaced and properly filled as part of the following update
            spatialCache.get(bbox).also {
                result = MutableMapDataWithGeometry(min(it.size * 2, 1000))
                it.forEach { result.put(it, ElementPointGeometry(it.position)) }
            }

            // fetch needed data and put it to cache
            tilesRectsToFetch.forEach { tilesRect ->
                val fetchBBox = tilesRect.asBoundingBox(tileZoom)
                val (elements, geometries) = fetchMapData(fetchBBox)
                update(updatedElements = elements, updatedGeometries = geometries, bbox = fetchBBox)
                if (fetchBBox == bbox) {
                    // return data if we need exactly the bbox that was just fetched
                    result.putAll(elements, geometries + elements.filterIsInstance<Node>().map { it.toElementGeometryEntry() })
                    result.boundingBox = bbox
                    return result
                }
                // add newly fetched nodes from elements
                // getting nodes from spatialCache can cause issues, as tiles in the bbox may now be removed unexpectedly
                // see https://github.com/streetcomplete/StreetComplete/issues/4980#issuecomment-1531960544
                for (element in elements) {
                    if (element !is Node) continue
                    if (element.position in bbox) result.put(element, ElementPointGeometry(element.position))
                }
            }
        } else {
            spatialCache.get(bbox).also {
                result = MutableMapDataWithGeometry(it.size)
                it.forEach { result.put(it, ElementPointGeometry(it.position)) }
            }
        }
        result.boundingBox = bbox

        val wayKeys = HashSet<ElementKey>(result.nodes.size / 5)
        val relationKeys = HashSet<ElementKey>(result.nodes.size / 10)
        for (node in result.nodes) {
            wayKeyByNodeKeyCache[node.key]?.let { wayKeys.addAll(it) }
            relationKeysByElementKeyCache[node.key]?.let { relationKeys.addAll(it) }
        }

        val nodesToFetch = hashSetOf<Long>()
        for (wayKey in wayKeys) {
            val way = notSpatialCache[wayKey] as Way
            val wayGeometry = wayRelationGeometryCache[wayKey]
            result.put(way, wayGeometry)
            relationKeysByElementKeyCache[way.key]?.let { relationKeys.addAll(it) }

            // find all nodes that are part of the way, but not in result
            if (wayGeometry?.getBounds()?.isCompletelyInside(bbox) == true) continue // no need to check
            for (nodeId in way.nodeIds) {
                if (result.getNode(nodeId) != null) continue
                val cachedNode = getCachedNode(ElementKey(ElementType.NODE, nodeId))
                if (cachedNode != null) {
                    result.put(cachedNode, ElementPointGeometry(cachedNode.position))
                    continue
                }
                nodesToFetch.add(nodeId)
            }
        }
        if (nodesToFetch.isNotEmpty()) {
            fetchNodes(nodesToFetch).forEach {
                notSpatialCache[it.key] = it
                result.put(it, ElementPointGeometry(it.position))
            }
        }

        for (relationKey in relationKeys) {
            result.put(notSpatialCache[relationKey]!!, wayRelationGeometryCache[relationKey])
            // don't add relations of relations, because elementDao.getAll(bbox) also isn't doing that
        }

        // trim if we fetched new data, and spatialCache is full
        // trim to 66%, so trim is (probably) not immediately called on next fetch
        if (tilesRectsToFetch != null && spatialCache.size >= maxTiles) {
            trim((maxTiles * 2) / 3)
        }
        return result
    }

    /** Clears the cache */
    fun clear() { synchronized(this) {
        Log.i(TAG, "clear cache")
        spatialCache.clear()
        notSpatialCache.clear()
        wayRelationGeometryCache.clear()
        wayKeyByNodeKeyCache.clear()
        relationKeysByElementKeyCache.clear()
    } }

    /** Reduces cache size to the given number of non-empty [tiles], and removes all data
     *  not contained in the remaining tiles.
     */
    fun trim(tiles: Int) { synchronized(this) {
        Log.i(TAG, "trim to $tiles tiles")

        // spatialCache does not remove tiles contained in noTrim
        spatialCache.trim(tiles)

        // ways and relations with at least one element in cache should not be removed
        val (wayIds, relationIds) = determineWayAndRelationIdsWithElementsInSpatialCache()

        notSpatialCache.keys.retainAll {
            when (it.type) {
                ElementType.WAY -> it.id in wayIds
                ElementType.RELATION -> it.id in relationIds
                else -> false
            }
        }
        wayRelationGeometryCache.keys.retainAll {
            when (it.type) {
                ElementType.WAY -> it.id in wayIds
                ElementType.RELATION -> it.id in relationIds
                else -> false
            }
        }

        // now clean up wayIdsByNodeIdCache and relationIdsByElementKeyCache
        wayKeyByNodeKeyCache.keys.retainAll { spatialCache.get(it) != null }
        relationKeysByElementKeyCache.keys.retainAll {
            when (it.type) {
                ElementType.NODE -> spatialCache.get(ElementKey(ElementType.NODE, it.id)) != null
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

        val wayIds = HashSet<Long>(notSpatialCache.size)
        for (way in notSpatialCache.values) {
            if (way !is Way) continue
            if (way.nodeIds.any { spatialCache.get(ElementKey(ElementType.NODE, it)) != null }) {
                wayIds.add(way.id)
            }
        }

        fun RelationMember.isCachedWayOrNode(): Boolean =
            type == ElementType.NODE && spatialCache.get(key) != null
                || type == ElementType.WAY && ref in wayIds

        fun RelationMember.hasCachedMembers(): Boolean =
            type == ElementType.RELATION
                && (notSpatialCache[key] as? Relation)?.members?.any { it.isCachedWayOrNode() } == true

        val relationIds = HashSet<Long>(notSpatialCache.size / 3)
        for (relation in notSpatialCache.values) {
            if (relation !is Relation) continue
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

    private fun getCachedNode(key: ElementKey): Node? = spatialCache.get(key) ?: (notSpatialCache[key] as? Node)

    private val ElementGeometryEntry.reuseKey get() = key.let { notSpatialCache[it]?.key ?: it }

    companion object {
        private const val TAG = "MapDataCache"
    }
}
