package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.download.tiles.minTileRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isCompletelyInside
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// inspired by Android LruCache
class QuestControllerCache(private val maxSize: Int, private val fetch: (BoundingBox, Collection<String>?) -> List<OsmQuest>) : LinkedHashMap<TilePos, MutableMap<OsmQuestKey, OsmQuest>>(0, 0.75f, true) {
    private val TILE_ZOOM = 16
    private var visibleQuests: HashSet<String>? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // don't count empty tiles when getting cache size as they only use negligible memory
    override val size get() = synchronized(this) { super.size - values.filter { it.isEmpty() }.size }

    fun remove(keys: Collection<OsmQuestKey>) {
        synchronized(this) {
            values.forEach { map ->
                // definitely fast enough for single quests
                keys.forEach {
                    map.remove(it)
                }
            }
        }
    }

    fun add(quests: Collection<OsmQuest>, replaceBBox: BoundingBox?) {
        // add all tiles that are fully in bbox to cache
        add(quests, replaceBBox?.enclosingTilesRect(TILE_ZOOM)?.asTilePosSequence()?.filter {
            it.asBoundingBox(TILE_ZOOM).isCompletelyInside(replaceBBox)
        }?.toList())
    }

    fun add(quests: Collection<OsmQuest>, tilesToReplace: Collection<TilePos>? = null) {
        // replace / add tiles to cache. if this is null, quests will only be added if tile is already in cache
        // quests that are outside these tiles (or outside cached tiles if tilesToReplace is null) will be ignored
        tilesToReplace?.forEach { put(it, mutableMapOf()) }

        synchronized(this) {
            // performance is not great, but acceptable. optimization would be good
            quests.forEach { q ->
                // ignore quest if not visible
                if (visibleQuests?.contains(q.questTypeName) == false) return@forEach

                val tile = q.position.enclosingTilePos(TILE_ZOOM)
                if (tilesToReplace?.contains(tile) != false)
                    get(tile)?.put(q.key, q)
            }
        }
    }

    fun getQuest(questKey: OsmQuestKey): OsmQuest? {
        synchronized(this) {
            values.forEach { tileMap ->
                tileMap[questKey]?.let { return it }
            }
        }
        return null
    }

    fun get(bbox: BoundingBox, questTypes: Collection<String>?): List<OsmQuest> {
        val requiredTiles = bbox.enclosingTilesRect(TILE_ZOOM).asTilePosSequence().toList()
        val quests = mutableSetOf<OsmQuest>()
        synchronized(this) {
            // drop cache if quest types changed
            val hs = questTypes?.toHashSet()
            if (visibleQuests != hs) {
                clear()
                visibleQuests = hs
            }

            // get what we have in cache
            requiredTiles.filter { containsKey(it) }.forEach { tile ->
                get(tile)!!.let {
                    if (tile.asBoundingBox(TILE_ZOOM).isCompletelyInside(bbox))
                        quests.addAll(it.values)
                    else
                        quests.addAll(it.values.filter { q -> bbox.contains(q.position) })
                }
            }
        }

        // fetch tiles not in cache
        val tilesToFetch = requiredTiles.filterNot { containsKey(it) }

        if (tilesToFetch.isEmpty()) {
            return quests.toList()
        }

        // now get missing quests inside minTileRect of tilesToFetch
        // todo: try making things more efficient if missing tiles can be arranged as 2 bboxes
        //  see https://github.com/streetcomplete/StreetComplete/issues/4079#issuecomment-1152616022
        //  but this may not be worth the effort
        val missingQuests = fetch(tilesToFetch.minTileRect()!!.asBoundingBox(TILE_ZOOM), questTypes)
        quests.addAll(missingQuests)

        scope.launch {
            // add fetched tiles to cache, but in background
            add(missingQuests, tilesToFetch)
        }
        return quests.toList()
    }

    // remove first (i.e. least recently accessed) entry until target size is reached
    fun trimToSize(max: Int) {
        if (max < 0)
            throw(IllegalArgumentException("can't trim to negative size"))
        synchronized(this) {
            while (size > max) {
                remove(keys.first())
            }
        }
    }

    // call trim on each put
    // todo: maybe it would be better call trim only once, as put it only called in add?
    override fun put(key: TilePos, value: MutableMap<OsmQuestKey, OsmQuest>): MutableMap<OsmQuestKey, OsmQuest>? {
        val r: MutableMap<OsmQuestKey, OsmQuest>?
        synchronized(this) {
            r = super.put(key, value)
            trimToSize(maxSize)
        }
        return r
    }

}
