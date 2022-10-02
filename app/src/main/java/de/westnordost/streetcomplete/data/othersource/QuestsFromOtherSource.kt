package de.westnordost.streetcomplete.data.othersource

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.OtherSourceQuestHiddenKey
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.quest.AllCountries
import de.westnordost.streetcomplete.data.quest.Countries
import de.westnordost.streetcomplete.data.quest.OtherSourceQuestKey
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.util.ktx.intersects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.FutureTask

val otherSourceModule = module {
    single { OtherSourceQuestController(get(named("CountryBoundariesFuture")), get(), get(), get()) }
    single { OsmoseDao(get(), get()) }
}

// todo: for a start, add external and osmose quests here!
//  external: allow specifying coordinates instead of element, though not sure what to do them
//   just show message, and maybe allow creating a node with pre-defined tags?

// maybe split into more classes?
class OtherSourceQuestController(
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>,
    questTypeRegistry: QuestTypeRegistry,
    private val otherSourceDao: OtherSourceDao,
    elementEditsController: ElementEditsController,
) : ElementEditsSource.Listener {

    private val hiddenCache = otherSourceDao.getAllHidden().toHashSet()

    interface QuestListener {
        fun onUpdated(addedQuests: Collection<OtherSourceQuest> = emptyList(), deletedQuestKeys: Collection<OtherSourceQuestKey> = emptyList())
        fun onInvalidate()
    }
    private val questListeners: MutableList<QuestListener> = CopyOnWriteArrayList()
    fun addQuestListener(questListener: QuestListener) {
        questListeners.add(questListener)
    }
    fun removeQuestListener(questListener: QuestListener) {
        questListeners.remove(questListener)
    }

    private val questTypes = questTypeRegistry.filterIsInstance<OtherSourceQuestType>().associateBy { it.source }
    init {
        if (questTypes.size != questTypeRegistry.filterIsInstance<OtherSourceQuestType>().size)
            throw IllegalStateException("source values must be unique")
        elementEditsController.addListener(this)
    }

    fun delete(key: OtherSourceQuestKey) {
        val type = questTypes[key.source] ?: return
        if (type.deleteQuest(key.id))
            questListeners.forEach { it.onUpdated(deletedQuestKeys = listOf(key)) }
    }

    fun getAllVisibleInBBox(bbox: BoundingBox, visibleQuestTypeNames: List<String>? = null): List<OtherSourceQuest> {
        val quests = (visibleQuestTypeNames?.filterIsInstance<OtherSourceQuestType>() ?: questTypes.values).flatMap {
            it.getQuests(bbox)
        }
        return quests.filterNot { it.key in hiddenCache }
    }

    fun get(key: OtherSourceQuestKey): OtherSourceQuest? {
        if (key in hiddenCache) return null
        val type = questTypes[key.source] ?: return null
        return type.get(key.id)
    }

    /** calls [download] for each [OtherSourceQuestType] enabled in this country, thus may take long */
    suspend fun download(bbox: BoundingBox) {
        withContext(Dispatchers.IO) {
            val countryBoundaries = countryBoundariesFuture.get()
            val obsoleteQuestKeys = mutableListOf<OtherSourceQuestKey>()
            val newQuests = mutableListOf<OtherSourceQuest>()

            questTypes.values.forEach { type ->
                if (!countryBoundaries.intersects(bbox, type.enabledInCountries)) return@forEach
                val previousQuests = type.getQuests(bbox).map { it.key }
                val quests = type.download(bbox)
                newQuests.addAll(quests)
                val questKeys = HashSet<OtherSourceQuestKey>(quests.size).apply { quests.forEach { add(it.key) } }
                obsoleteQuestKeys.addAll(previousQuests.filterNot { it in questKeys })
            }
            questListeners.forEach { it.onUpdated(newQuests, obsoleteQuestKeys) }
        }
    }

    /** calls [upload] for each [OtherSourceQuestType], thus may take long */
    fun upload() = questTypes.values.forEach { it.upload() }

    fun invalidate() = questListeners.forEach { it.onInvalidate() }


    // hiding / unhiding

    // tempHide is not really hiding, and is also used so pins actually disappear when quest is solved
    fun tempHide(key: OtherSourceQuestKey) {
        questListeners.forEach { it.onUpdated(deletedQuestKeys = listOf(key)) }
    }

    interface HideQuestListener {
        fun onHid(edit: OtherSourceQuestHidden)
        fun onUnhid(edit: OtherSourceQuestHidden)
        fun onUnhidAll()
    }
    private val hideListeners: MutableList<HideQuestListener> = CopyOnWriteArrayList()
    fun addHideListener(hideQuestListener: HideQuestListener) {
        hideListeners.add(hideQuestListener)
    }
    fun removeHideListener(hideQuestListener: HideQuestListener) {
        hideListeners.remove(hideQuestListener)
    }

    fun hide(key: OtherSourceQuestKey) {
        val type = questTypes[key.source] ?: return
        val quest = get(key) ?: return
        hiddenCache.add(key)
        val timestamp = otherSourceDao.hide(key)
        if (timestamp > 0) {
            val hiddenQuest = OtherSourceQuestHidden(key.id, type, quest.position, timestamp)
            hideListeners.forEach { it.onHid(hiddenQuest) }
        }
    }

    fun unhide(key: OtherSourceQuestKey): Boolean {
        if (!hiddenCache.remove(key)) return false
        val hiddenQuest = getHidden(key) ?: return false
        if (!otherSourceDao.unhide(key)) return false
        hideListeners.forEach { it.onHid(hiddenQuest) }
        return true
    }

    fun unhideAll(): Int {
        hiddenCache.clear()
        val count = otherSourceDao.unhideAll()
        hideListeners.forEach { it.onUnhidAll() }
        return count
    }

    fun getHidden(key: OtherSourceQuestKey, timestamp: Long? = null): OtherSourceQuestHidden? {
        val ts = timestamp ?: otherSourceDao.getHiddenTimestamp(key) ?: return null
        val quest = questTypes[key.source]?.get(key.id) ?: return null
        return OtherSourceQuestHidden(quest.id, quest.type, quest.position, ts)
    }

    fun getAllHiddenNewerThan(timestamp: Long): List<OtherSourceQuestHidden> {
        val hiddenKeys = otherSourceDao.getAllHiddenNewerThan(timestamp)
        return hiddenKeys.mapNotNull { getHidden(it.first, it.second) }
    }

    // ElementEditsListener

    override fun onAddedEdit(edit: ElementEdit) {} // ignore, and actually this should never be called

    override fun onAddedEdit(edit: ElementEdit, key: QuestKey) {
        if (key is OtherSourceQuestKey)
            otherSourceDao.addElementEdit(key, edit.id)
    }

    override fun onSyncedEdit(edit: ElementEdit) {
        val type = edit.type as? OtherSourceQuestType ?: return
        val key = otherSourceDao.getKeyForElementEdit(edit.id)
        type.onSyncedEdit(edit, key?.id)
    }

    // for undoing stuff
    override fun onDeletedEdits(edits: List<ElementEdit>) {
        for (edit in edits) {
            val type = edit.type as? OtherSourceQuestType ?: continue
            val key = otherSourceDao.getKeyForElementEdit(edit.id)
            type.onDeletedEdit(edit, key?.id)
        }
    }
}

// todo: if a quest doesn't lead to an elementEdit, currently there is no way to undo
//  -> implement for things like undo reportFalsePositive
/*
class OtherSourceEditKey(val source: String, val id: Long) : EditKey() // have a key for each source that needs it?
data class OtherSourceEdit(
    override val position: LatLon,
    override val isSynced: Boolean?,
    val action: Unit, // depending on the source and what was done, need some type...
) : Edit {
    override val key: OtherSourceEditKey
    override val createdTimestamp: Long
    override val isUndoable: Boolean
}
*/

data class OtherSourceQuest(
    /** Each quest must be uniquely identified by the [id] and [source] */
    val id: String,
    override val geometry: ElementGeometry,
    override val type: OtherSourceQuestType,
    ) : Quest {
    override val key by lazy { OtherSourceQuestKey(id, source) }
    override val markerLocations: Collection<LatLon> get() = listOf(geometry.center)
    override val position: LatLon get() = geometry.center
    val source get() = type.source
}

// do it very similar to OsmElementQuestType
// for cleanup, each quest type should override deleteMetadataOlderThan, or old data will remain
interface OtherSourceQuestType : QuestType, ElementEditType {
    // like for OsmQuestType
    override val title: Int get() = getTitle(emptyMap())
    fun getTitle(tags: Map<String, String>): Int
    fun getTitleArgs(tags: Map<String, String>): Array<String> = arrayOf()
    val highlightedElementsRadius: Double? get() = null
    fun getHighlightedElements(getMapData: () -> MapDataWithGeometry): Sequence<Element> = emptySequence()
    val enabledInCountries: Countries get() = AllCountries

    /** Unique string for each source (app will crash on start if sources are not unique). */
    val source: String

    /** Download and persist data, create quests inside the given bbox and return the new quests. */
    fun download(bbox: BoundingBox): Collection<OtherSourceQuest>

    /**
     *  Upload changes to the server. Uploaded quests should not be created again on [download].
     *  Note that on each individual upload, [onSyncedEdit] will be called if there is a connected
     *  ElementEdit.
     */
    fun upload()

    /** Return all quests inside the given [bbox]. */
    fun getQuests(bbox: BoundingBox): Collection<OtherSourceQuest>

    /** Return quest with the given [id], or null. */
    fun get(id: String): OtherSourceQuest?

    /** Called if the ElementEdit done as part of quest with the given [id] was deleted (undone)
     *  [id] can be null in case edit was not properly associated with id.
     */
    fun onDeletedEdit(edit: ElementEdit, id: String?)

    /**
     *  Called if the ElementEdit done as part of quest with the given [id] was synced (uploaded).
     *  [id] can be null in case edit was not properly associated with id.
     *  Note that [upload] will also be called (before the first edit upload).
     */
    fun onSyncedEdit(edit: ElementEdit, id: String?)

    /**
     * Removes the quest with the given [id]. What happens internally doesn't matter, as long as
     * the quest doesn't show up again when using [get] or [getQuests].
     */
    fun deleteQuest(id: String): Boolean

    /**
     * Necessary to clean old data.
     * Will be called with (nearly) current time when clearing all stored data is desired.
    */
    override fun deleteMetadataOlderThan(timestamp: Long)
}

data class OtherSourceQuestHidden(
    val id: String,
    val questType: OtherSourceQuestType,
    override val position: LatLon,
    override val createdTimestamp: Long
) : Edit {
    val questKey get() = OtherSourceQuestKey(id, questType.source)
    override val key: OtherSourceQuestHiddenKey get() = OtherSourceQuestHiddenKey(questKey)
    override val isUndoable: Boolean get() = true
    override val isSynced: Boolean? get() = null
}
