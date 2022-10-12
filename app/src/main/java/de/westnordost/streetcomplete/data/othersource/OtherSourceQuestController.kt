package de.westnordost.streetcomplete.data.othersource

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.quest.OtherSourceQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.util.ktx.intersects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.FutureTask

class OtherSourceQuestController(
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>,
    private val questTypeRegistry: QuestTypeRegistry,
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

    private val questTypes get() = questTypeRegistry.filterIsInstance<OtherSourceQuestType>()
    private val questTypeNamesBySource = questTypes.associate { it.source to it.name }
    init {
        if (questTypes.size != questTypeNamesBySource.size)
            throw IllegalStateException("source values must be unique")
        elementEditsController.addListener(this)
    }

    fun delete(key: OtherSourceQuestKey) {
        if (getQuestType(key)?.deleteQuest(key.id) == true)
            questListeners.forEach { it.onUpdated(deletedQuestKeys = listOf(key)) }
    }

    fun getAllInBBox(bbox: BoundingBox, visibleQuestTypes: List<QuestType>? = null, getHidden: Boolean = false): List<OtherSourceQuest> {
        val quests = (visibleQuestTypes?.filterIsInstance<OtherSourceQuestType>() ?: questTypes).flatMap {
            it.getQuests(bbox)
        }
        return if (getHidden) quests else quests.filterNot { it.key in hiddenCache }
    }

    fun get(key: OtherSourceQuestKey): OtherSourceQuest? {
        if (key in hiddenCache) return null
        return getQuestType(key)?.get(key.id)
    }

    /** calls [download] for each [OtherSourceQuestType] enabled in this country, thus may take long */
    suspend fun download(bbox: BoundingBox) {
        withContext(Dispatchers.IO) {
            val countryBoundaries = countryBoundariesFuture.get()
            val obsoleteQuestKeys = mutableListOf<OtherSourceQuestKey>()
            val newQuests = mutableListOf<OtherSourceQuest>()

            questTypes.forEach { type ->
                if (!type.downloadEnabled) return@forEach
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
    fun upload() = questTypes.forEach { it.upload() }

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
        val type = getQuestType(key) ?: return
        val quest = get(key) ?: return
        hiddenCache.add(key)
        val timestamp = otherSourceDao.hide(key)
        if (timestamp > 0) {
            val hiddenQuest = OtherSourceQuestHidden(key.id, type, quest.position, timestamp)
            hideListeners.forEach { it.onHid(hiddenQuest) }
            questListeners.forEach { it.onUpdated(deletedQuestKeys = listOf(key)) }
        }
    }

    fun unhide(key: OtherSourceQuestKey): Boolean {
        if (!hiddenCache.remove(key)) return false
        val hiddenQuest = getHidden(key) ?: return false
        if (!otherSourceDao.unhide(key)) return false
        hideListeners.forEach { it.onUnhid(hiddenQuest) }
        get(key)?.let { q -> questListeners.forEach { it.onUpdated(addedQuests = listOf(q)) } }
        return true
    }

    fun unhideAll(): Int {
        hiddenCache.clear()
        val count = otherSourceDao.unhideAll()
        hideListeners.forEach { it.onUnhidAll() }
        // no need to call onInvalidated on the listeners, OsmQuestController is already doing this
        return count
    }

    fun getHidden(key: OtherSourceQuestKey, timestamp: Long? = null): OtherSourceQuestHidden? {
        val ts = timestamp ?: otherSourceDao.getHiddenTimestamp(key) ?: return null
        val quest = getQuestType(key)?.get(key.id) ?: return null
        return OtherSourceQuestHidden(quest.id, quest.type, quest.position, ts)
    }

    fun getAllHiddenNewerThan(timestamp: Long): List<OtherSourceQuestHidden> {
        val hiddenKeys = otherSourceDao.getAllHiddenNewerThan(timestamp)
        return hiddenKeys.mapNotNull { getHidden(it.first, it.second) }
    }

    // ElementEditsListener

    // ignore, and actually this should never be called for OtherSourceQuestType
    override fun onAddedEdit(edit: ElementEdit) {}

    override fun onAddedEdit(edit: ElementEdit, key: QuestKey?) {
        if (key is OtherSourceQuestKey) {
            getQuestType(key)?.onAddedEdit(edit, key.id)
            otherSourceDao.addElementEdit(key, edit.id)
        }
    }

    override fun onSyncedEdit(edit: ElementEdit) {
        val type = edit.type as? OtherSourceQuestType ?: return
        val key = otherSourceDao.getKeyForElementEdit(edit.id)
        // don't delete synced edits, this will be done by ElementEditsController (using onDeletedEdits)
        type.onSyncedEdit(edit, key?.id)
    }

    // for undoing stuff
    override fun onDeletedEdits(edits: List<ElementEdit>) {
        val restoredQuests = edits.mapNotNull { edit ->
            val type = edit.type as? OtherSourceQuestType ?: return@mapNotNull null
            val key = otherSourceDao.getKeyForElementEdit(edit.id)
            otherSourceDao.deleteElementEdit(edit.id)
            type.onDeletedEdit(edit, key?.id)
            if (key == null) null
            else get(key)
        }
        questListeners.forEach { it.onUpdated(addedQuests = restoredQuests) }
    }

    private fun getQuestType(key: OtherSourceQuestKey): OtherSourceQuestType? {
        val questTypeName = questTypeNamesBySource[key.source] ?: return null
        return questTypeRegistry.getByName(questTypeName) as? OtherSourceQuestType
    }
}
