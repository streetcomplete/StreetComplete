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

    /**
     * Removes elements and geometries with keys in [deletedKeys] from cache.
     * Puts the [updatedElements] and [updatedGeometries] into cache. Nodes are handled by
     * [spatialCache] and may not be put.
     */
    fun update(
        deletedKeys: Collection<ElementKey> = emptyList(),
        updatedElements: Iterable<Element> = emptyList(),
        updatedGeometries: Iterable<ElementGeometryEntry> = emptyList(),
        bbox: BoundingBox? = null
    ) = synchronized(this) {
        if (bbox == null)
            spatialCache.update(
                updatedOrAdded = updatedElements.filterIsInstance<Node>(),
                deleted = deletedKeys.mapNotNull { if (it.type == ElementType.NODE) it.id else null }
            )
        else {
            if (deletedKeys.isNotEmpty()) spatialCache.update(deleted = deletedKeys.mapNotNull { if (it.type == ElementType.NODE) it.id else null })
            spatialCache.replaceAllInBBox(updatedElements.filterIsInstance<Node>(), bbox)
        }

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

        updatedGeometries.forEach {
            if (it.elementType != ElementType.NODE)
                wayRelationGeometryCache[ElementKey(it.elementType, it.elementId)] = it.geometry
        }

        val updatedWays = updatedElements.filterIsInstance<Way>()
        val updatedRelations = updatedElements.filterIsInstance<Relation>()

        updatedWays.forEach { way ->
            val key = ElementKey(ElementType.WAY, way.id)
            wayRelationCache[key]?.let { oldWay ->
                // remove old way from wayIdsByNodeIdCache
                (oldWay as Way).nodeIds.forEach {
                    wayIdsByNodeIdCache[it]?.remove(way.id)
                }
            }
            wayRelationCache[key] = way

            way.nodeIds.forEach {
                // add to wayIdsByNodeIdCache if node is in spatialCache
                if (spatialCache.get(it) != null) {
                    wayIdsByNodeIdCache.getOrPut(it) { ArrayList(2) }.add(way.id)
                } else {
                    // But if we already have an entry for that nodeId (cached from getWaysForNode),
                    // we definitely need to add the updated way
                    wayIdsByNodeIdCache[it]?.add(way.id)
                }
            }
        }

        if (updatedRelations.isEmpty())
            return // no need to create way and relation id lists

        // for adding relations to relationIdsByElementKeyCache we want the element to be
        // in spatialCache, or have a node / member in spatialCache (same reasoning as for ways)
        val (wayIds, relationIds) = getWayAndRelationIdsWithElementsInSpatialCache()

        updatedRelations.forEach { relation ->
            val key = ElementKey(ElementType.RELATION, relation.id)
            wayRelationCache[key]?.let { oldRelation ->
                // remove old relation from relationIdsByElementKeyCache
                (oldRelation as Relation).members.forEach {
                    relationIdsByElementKeyCache[ElementKey(it.type, it.ref)]?.remove(relation.id)
                }
            }
            wayRelationCache[key] = relation

            relation.members.forEach {
                val memberKey = ElementKey(it.type, it.ref)
                // add to relationIdsByElementKeyCache if member is in spatialCache or has nodes or
                // members in spatialCache
                if ((it.type == ElementType.NODE && spatialCache.get(it.ref) != null)
                        || (it.ref in wayIds && it.type == ElementType.WAY)
                        || (it.ref in relationIds && it.type == ElementType.RELATION)) {
                    relationIdsByElementKeyCache.getOrPut(memberKey) { ArrayList(2) }.add(relation.id)
                } else {
                    // But if we already have an entry for that elementKey (cached from getRelationsForElement),
                    // we definitely need to add the updated relation
                    relationIdsByElementKeyCache[memberKey]?.add(relation.id)
                }
            }
        }
    }

    /**
     * Gets element from cache. If element is not cached, [fetch] is called, and the result is
     * cached and then returned.
     */
    fun getElement(type: ElementType, id: Long, fetch: (ElementType, Long) -> Element?): Element? = synchronized(this) {
        val element =
            if (type == ElementType.NODE) spatialCache.get(id)
            else wayRelationCache.getOrPutIfNotNull(ElementKey(type, id)) { fetch(type, id) }
        return element ?: fetch(type, id)
    }

    /**
     * Gets geometry from cache. If geometry is not cached, [fetch] is called, and the result is
     * cached and then returned.
     */
    fun getGeometry(type: ElementType, id: Long, fetch: (ElementType, Long) -> ElementGeometry?): ElementGeometry? =synchronized(this)  {
        val geometry = if (type == ElementType.NODE) spatialCache.get(id)
            ?.let { ElementPointGeometry(it.position) }
        else wayRelationGeometryCache.getOrPutIfNotNull(ElementKey(type, id)) { fetch(type, id) }
        return geometry ?: fetch(type, id)
    }

    /**
     * Gets elements from cache. If any of the elements is not cached, [fetch] is called for the
     * missing elements. The fetched elements are cached and the complete list is returned.
     */
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

    fun getNodes(ids: Collection<Long>, fetch: (Collection<Long>) -> List<Node>): List<Node> = synchronized(this) {
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

    /**
     * Gets geometries from cache. If any of the geometries is not cached, [fetch] is called for the
     * missing geometries. The fetched geometries are cached and the complete list is returned.
     */
    fun getGeometries(
        keys: Collection<ElementKey>,
        fetch: (Collection<ElementKey>) -> List<ElementGeometryEntry>
    ): List<ElementGeometryEntry> = synchronized(this) {
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

    /**
     * Gets all ways for node [id] from cache. If the list of ways is not known, or any way is
     * missing in cache, [fetch] is called and the fetched list of ways and all ways are put to cache.
     */
    fun getWaysForNode(id: Long, fetch: (Long) -> List<Way>): List<Way> = synchronized(this) {
        val wayIds = wayIdsByNodeIdCache.getOrPut(id) {
            val ways = fetch(id)
            for (way in ways) { wayRelationCache[ElementKey(ElementType.WAY, way.id)] = way }
            ways.map { it.id }.toMutableList()
        }
        return wayIds.map { wayRelationCache[ElementKey(ElementType.WAY, it)] as Way }
    }

    /**
     * Gets all relations for node [id] from cache. If the list of relations is not known, or any
     * relations is missing in cache, [fetch] is called and the fetched list of relations and
     * all relations are put to cache.
     */
    fun getRelationsForNode(id: Long, fetch: (Long) -> List<Relation>) =
        getRelationsForElement(ElementType.NODE, id) { fetch(id) }
    /**
     * Gets all relations for way [id] from cache. If the list of relations is not known, or any
     * relations is missing in cache, [fetch] is called and the fetched list of relations and
     * all relations are put to cache.
     */
    fun getRelationsForWay(id: Long, fetch: (Long) -> List<Relation>) =
        getRelationsForElement(ElementType.WAY, id) { fetch(id) }
    /**
     * Gets all relations for relation [id] from cache. If the list of relations is not known, or any
     * relations is missing in cache, [fetch] is called and the fetched list of relations and
     * all relations are put to cache.
     */
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

    /**
     * Gets all elements and geometries inside [bbox]. This returns all nodes, all ways containing
     * at least one of the nodes, and all relations containing at least one of the ways or nodes,
     * and the geometries.
     * If data is not cached, tiles containing the [bbox] are fetched from database and cached.
     */
    fun getMapDataWithGeometry(bbox: BoundingBox): MutableMapDataWithGeometry = synchronized(this) {
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
            // duplicate fetch might be unnecessary in many cases, but it's very fast anyway
            nodes = HashSet<Node>().apply { addAll(spatialCache.get(bbox)) }

            update(updatedElements = elements, updatedGeometries = geometries, bbox = fetchBBox)

            // return data if we need exactly what was just fetched
            if (fetchBBox == bbox) {
                result.putAll(elements, geometries + elements.filterIsInstance<Node>().map { it.toElementGeometryEntry() })
                return result
            }

            // get nodes again, this contains the newly added nodes, but maybe not the old ones if cache was trimmed
            nodes.addAll(spatialCache.get(bbox))
        } else {
            nodes = spatialCache.get(bbox)
        }

        val wayIds = HashSet<Long>(nodes.size / 5)
        val relationIds = HashSet<Long>(nodes.size / 10)
        nodes.forEach { node ->
            wayIdsByNodeIdCache[node.id]?.let { wayIds.addAll(it) }
            relationIdsByElementKeyCache[ElementKey(ElementType.NODE, node.id)]?.let { relationIds.addAll(it) }
            result.put(node, ElementPointGeometry(node.position))
        }
        wayIds.forEach { wayId ->
            val key = ElementKey(ElementType.WAY, wayId)
            result.put(wayRelationCache[key]!!, wayRelationGeometryCache[key])
            relationIdsByElementKeyCache[ElementKey(ElementType.WAY, wayId)]?.let { relationIds.addAll(it) }
        }
        relationIds.forEach { relationId ->
            val key = ElementKey(ElementType.RELATION, relationId)
            result.put(wayRelationCache[key]!!, wayRelationGeometryCache[key])
            // don't add relations of relations, because elementDao.getAll(bbox) also isn't doing that
        }

        // trim if we fetched new data, and spatialCache is full
        // trim to 90%, so trim is (probably) not immediately called on next fetch
        if (spatialCache.size >= maxTiles && tilesToFetch.isNotEmpty())
            trim((maxTiles * 9) / 10)
        return result
    }

    /** Clears the cache */
    fun clear() = synchronized(this) {
        spatialCache.clear()
        wayRelationCache.clear()
        wayRelationGeometryCache.clear()
        wayIdsByNodeIdCache.clear()
        relationIdsByElementKeyCache.clear()
    }

    /** Reduces cache size to the given number of non-empty [tiles], and removes all data
     * not contained in the remaining tiles.
     */
    fun trim(tiles: Int) = synchronized(this) {
        spatialCache.trim(tiles)
        trimNonSpatialCaches()
    }

    private fun trimNonSpatialCaches() {
        synchronized(this) {
            // ways and relations with at least one element in cache should not be removed
            val (wayIds, relationIds) = getWayAndRelationIdsWithElementsInSpatialCache()

            wayRelationCache.keys.retainAll {
                if (it.type == ElementType.RELATION)
                    it.id in relationIds
                else it.id in wayIds
            }
            wayRelationGeometryCache.keys.retainAll {
                if (it.type == ElementType.RELATION)
                    it.id in relationIds
                else it.id in wayIds
            }

            // now clean up wayIdsByNodeIdCache and relationIdsByElementKeyCache
            wayIdsByNodeIdCache.keys.retainAll { spatialCache.get(it) != null }
            relationIdsByElementKeyCache.keys.retainAll {
                (it.type == ElementType.NODE && spatialCache.get(it.id) != null)
                    || (it.type == ElementType.WAY && it.id in wayIds)
                    || (it.type == ElementType.RELATION && it.id in relationIds)
            }
        }
    }

    private fun getWayAndRelationIdsWithElementsInSpatialCache(): Pair<Set<Long>, Set<Long>> = synchronized(this) {
        val wayIds = wayRelationCache.values.mapNotNull { element ->
            if (element is Way && element.nodeIds.any { spatialCache.get(it) != null })
                element.id
            else null
        }.toHashSet()

        fun RelationMember.isCached(): Boolean =
            type == ElementType.NODE && spatialCache.get(ref) != null
                || type == ElementType.WAY && ref in wayIds

        val relationIds = wayRelationCache.values.mapNotNull { element ->
            if (element is Relation && element.members.any { member ->
                    member.isCached()
                        || (member.type == ElementType.RELATION // relation of relations
                            && (wayRelationCache[ElementKey(ElementType.RELATION, member.ref)] as? Relation)
                                ?.members?.any { it.isCached() } == true)
                })
                element.id
            else null
        }.toHashSet()
        wayIds to relationIds
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
