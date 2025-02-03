package de.westnordost.streetcomplete.data.externalsource

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.quest.ExternalSourceQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.util.ktx.intersects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList

class ExternalSourceQuestController(
    private val countryBoundaries: Lazy<CountryBoundaries>,
    private val questTypeRegistry: QuestTypeRegistry,
    private val externalSourceDao: ExternalSourceDao,
    elementEditsController: ElementEditsController,
) : ElementEditsSource.Listener {

    interface QuestListener {
        fun onUpdated(addedQuests: Collection<ExternalSourceQuest> = emptyList(), deletedQuestKeys: Collection<ExternalSourceQuestKey> = emptyList())
        fun onInvalidate()
    }
    private val questListeners: MutableList<QuestListener> = CopyOnWriteArrayList()
    fun addQuestListener(questListener: QuestListener) {
        questListeners.add(questListener)
    }

    private val questTypes get() = questTypeRegistry.filterIsInstance<ExternalSourceQuestType>()
    private val questTypeNamesBySource by lazy {
        val types = questTypes
        val namesBySource = types.associate { it.source to it.name }
        if (types.size != namesBySource.size)
            throw IllegalStateException("source values must be unique")
        namesBySource
    }
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init { elementEditsController.addListener(this) }

    fun delete(key: ExternalSourceQuestKey) {
        getQuestType(key)?.deleteQuest(key.id)
        questListeners.forEach { it.onUpdated(deletedQuestKeys = listOf(key)) }
    }

    fun getAllInBBox(bbox: BoundingBox, visibleQuestTypes: List<QuestType>? = null): List<ExternalSourceQuest> {
        return (visibleQuestTypes?.filterIsInstance<ExternalSourceQuestType>() ?: questTypes)
            .flatMap { it.getQuests(bbox) }
    }

    fun get(key: ExternalSourceQuestKey): ExternalSourceQuest? = getQuestType(key)?.get(key.id)

    /** calls [download] for each [ExternalSourceQuestType] enabled in this country, thus may take long */
    suspend fun download(bbox: BoundingBox) {
        withContext(Dispatchers.IO) {
            val countryBoundaries = countryBoundaries.value
            val updates = questTypes.mapNotNull { type ->
                if (!type.downloadEnabled) return@mapNotNull null
                if (!countryBoundaries.intersects(bbox, type.enabledInCountries)) return@mapNotNull null
                scope.async {
                    val previousQuests = type.getQuests(bbox).map { it.key }
                    val quests = type.download(bbox)
                    val questKeys = HashSet<ExternalSourceQuestKey>(quests.size).apply { quests.forEach { add(it.key) } }
                    quests to previousQuests.filterNot { it in questKeys }
                }
            }.awaitAll().unzip()
            val newQuests = updates.first.flatten()
            val obsoleteQuestKeys = updates.second.flatten()
            questListeners.forEach { it.onUpdated(newQuests, obsoleteQuestKeys) }
        }
    }

    /** calls [upload] for each [ExternalSourceQuestType], thus may take long */
    suspend fun upload() = questTypes.forEach { it.upload() }

    fun invalidate() = questListeners.forEach { it.onInvalidate() }

    /** to be called if quests have been added outside a download, so they can be shown immediately */
    fun addQuests(quests: Collection<ExternalSourceQuest>) =
        questListeners.forEach { it.onUpdated(addedQuests = quests) }

    // ElementEditsListener

    // ignore, and actually this should never be called for ExternalSourceQuestType
    override fun onAddedEdit(edit: ElementEdit) {}

    override fun onAddedEdit(edit: ElementEdit, key: QuestKey?) {
        if (key is ExternalSourceQuestKey) {
            getQuestType(key)?.onAddedEdit(edit, key.id)
            externalSourceDao.addElementEdit(key, edit.id)
            questListeners.forEach { it.onUpdated(deletedQuestKeys = listOf(key)) }
        }
    }

    override fun onSyncedEdit(edit: ElementEdit) {
        val type = edit.type as? ExternalSourceQuestType ?: return
        val key = externalSourceDao.getKeyForElementEdit(edit.id)
        // don't delete synced edits, this will be done by ElementEditsController (using onDeletedEdits)
        type.onSyncedEdit(edit, key?.id)
    }

    fun onSyncEditFailed(edit: ElementEdit) {
        val type = edit.type as? ExternalSourceQuestType ?: return
        val key = externalSourceDao.getKeyForElementEdit(edit.id)
        type.onSyncEditFailed(edit, key?.id)
    }

    suspend fun onUpload(edit: ElementEdit): Boolean {
        val type = edit.type as? ExternalSourceQuestType ?: return true
        val key = externalSourceDao.getKeyForElementEdit(edit.id)
        return type.onUpload(edit, key?.id)
    }

    // for undoing stuff
    override fun onDeletedEdits(edits: List<ElementEdit>) {
        val restoredQuests = edits.mapNotNull { edit ->
            if (edit.isSynced) return@mapNotNull null // synced edits are deleted after 12 hours, and we don't want this to restore anything
            val key = externalSourceDao.getKeyForElementEdit(edit.id)
            externalSourceDao.deleteElementEdit(edit.id)
            val type = questTypeNamesBySource[key?.source]?.let { questTypeRegistry.getByName(it) } as? ExternalSourceQuestType
            type?.onDeletedEdit(edit, key?.id)
            if (key == null) null
            else get(key)
        }
        questListeners.forEach { it.onUpdated(addedQuests = restoredQuests) }
    }

    // called when old elementEdits are removed
    fun cleanElementEdits(elementEditsIds: Collection<Long>) =
        externalSourceDao.deleteAllExceptForElementEdits(elementEditsIds)

    fun getQuestType(key: ExternalSourceQuestKey): ExternalSourceQuestType? {
        val questTypeName = questTypeNamesBySource[key.source] ?: return null
        return questTypeRegistry.getByName(questTypeName) as? ExternalSourceQuestType
    }
}
