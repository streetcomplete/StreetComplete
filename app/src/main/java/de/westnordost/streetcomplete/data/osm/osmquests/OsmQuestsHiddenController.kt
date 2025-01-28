package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.util.Listeners

/** Controller for managing which osm quests have been hidden by user interaction. */
class OsmQuestsHiddenController(
    private val db: OsmQuestsHiddenDao,
    private val mapDataSource: MapDataWithEditsSource,
    private val questTypeRegistry: QuestTypeRegistry,
) : OsmQuestsHiddenSource, HideOsmQuestController {

    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    private val listeners = Listeners<OsmQuestsHiddenSource.Listener>()

    /** Mark the quest as hidden by user interaction */
    override fun hide(key: OsmQuestKey) {
        db.add(key)
        val hidden = get(key)
        if (hidden != null) onHid(hidden)
    }

    /** Un-hide the given quest. Returns whether it was hid before */
    fun unhide(key: OsmQuestKey): Boolean {
        val hidden = get(key)
        if (!db.delete(key)) return false
        if (hidden != null) onUnhid(hidden)
        return true
    }

    /** Un-hides all previously hidden quests by user interaction */
    fun unhideAll(): Int {
        val unhidCount = db.deleteAll()
        onUnhidAll()
        return unhidCount
    }

    override fun isHidden(key: OsmQuestKey): Boolean {
        return db.contains(key)
    }

    override fun get(key: OsmQuestKey): OsmQuestHidden? {
        val timestamp = db.getTimestamp(key) ?: return null
        val geometry = mapDataSource.getGeometry(key.elementType, key.elementId) ?: return null
        return createOsmQuestHidden(key, geometry, timestamp)
    }

    override fun getAllNewerThan(timestamp: Long): List<OsmQuestHidden> {
        val questKeysWithTimestamp = db.getNewerThan(timestamp)

        val elementKeys = questKeysWithTimestamp.mapTo(HashSet()) {
            ElementKey(it.osmQuestKey.elementType, it.osmQuestKey.elementId)
        }

        val geometriesByKey = mapDataSource.getGeometries(elementKeys).associateBy { it.key }

        return questKeysWithTimestamp.mapNotNull { (key, timestamp) ->
            val geometry = geometriesByKey[ElementKey(key.elementType, key.elementId)]?.geometry
                ?: return@mapNotNull null
            createOsmQuestHidden(key, geometry, timestamp)
        }
    }

    override fun countAll(): Long = db.countAll()

    private fun createOsmQuestHidden(
        key: OsmQuestKey,
        geometry: ElementGeometry,
        timestamp: Long
    ): OsmQuestHidden? {
        val questType = questTypeRegistry.getByName(key.questTypeName) as? OsmElementQuestType<*> ?: return null
        return OsmQuestHidden(key.elementType, key.elementId, questType, geometry, timestamp)
    }

    override fun addListener(listener: OsmQuestsHiddenSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: OsmQuestsHiddenSource.Listener) {
        listeners.remove(listener)
    }

    private fun onHid(edit: OsmQuestHidden) {
        listeners.forEach { it.onHid(edit) }
    }
    private fun onUnhid(edit: OsmQuestHidden) {
        listeners.forEach { it.onUnhid(edit) }
    }
    private fun onUnhidAll() {
        listeners.forEach { it.onUnhidAll() }
    }
}
