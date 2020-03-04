package de.westnordost.streetcomplete.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log

import javax.inject.Inject

import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmElement
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.download.QuestDownloadService
import de.westnordost.streetcomplete.data.osm.ElementKey
import de.westnordost.streetcomplete.data.osm.OsmQuest
import de.westnordost.streetcomplete.data.osm.OsmQuestSplitWay
import de.westnordost.streetcomplete.data.osm.UndoOsmQuest
import de.westnordost.streetcomplete.data.osm.changes.SplitPolylineAtPosition
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestSplitWayDao
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestDao
import de.westnordost.streetcomplete.data.osmnotes.CreateNote
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteDao
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao
import de.westnordost.streetcomplete.data.upload.QuestChangesUploadService
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider
import de.westnordost.streetcomplete.quests.note_discussion.NoteAnswer
import de.westnordost.streetcomplete.util.SlippyMapMath
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element

import android.content.Context.BIND_AUTO_CREATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Singleton

@Singleton class QuestController @Inject constructor(
    private val osmQuestDB: OsmQuestDao,
    private val undoOsmQuestDB: UndoOsmQuestDao,
    private val osmElementDB: MergedElementDao,
    private val geometryDB: ElementGeometryDao,
    private val osmNoteQuestDB: OsmNoteQuestDao,
    private val splitWayDB: OsmQuestSplitWayDao,
    private val createNoteDB: CreateNoteDao,
    private val prefs: SharedPreferences,
    private val questTypesProvider: OrderedVisibleQuestTypesProvider,
    private val context: Context
) {

    private val listeners: MutableList<VisibleQuestListener> = CopyOnWriteArrayList()
    private val relay = object : VisibleQuestListener {
        override fun onQuestsCreated(quests: Collection<Quest>, group: QuestGroup) {
            for (listener in listeners) {
                listener.onQuestsCreated(quests, group)
            }
        }

        override fun onQuestsRemoved(questIds: Collection<Long>, group: QuestGroup) {
            for (listener in listeners) {
                listener.onQuestsRemoved(questIds, group)
            }
        }
    }

    private var downloadServiceIsBound: Boolean = false
    private var downloadService: QuestDownloadService.Interface? = null
    private val downloadServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            downloadService = service as QuestDownloadService.Interface
            downloadService?.setQuestListener(relay)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            downloadService = null
        }
    }

    private var uploadServiceIsBound: Boolean = false
    private var uploadService: QuestChangesUploadService.Interface? = null
    private val uploadServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            uploadService = service as QuestChangesUploadService.Interface
            uploadService?.setQuestListener(relay)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            uploadService = null
        }
    }

    private val questTypeNames get() = questTypesProvider.get().map { it.javaClass.simpleName }

    /** @return true if a quest download triggered by the user is running */
    val isPriorityDownloadInProgress: Boolean get() =
        downloadService?.isPriorityDownloadInProgress == true

    /** @return true if a quest download is running */
    val isDownloadInProgress: Boolean get() =
        downloadService?.isDownloadInProgress == true

    init {
        bindServices()
    }

    fun bindServices() {
        downloadServiceIsBound = context.bindService(
            Intent(context, QuestDownloadService::class.java),
            downloadServiceConnection, BIND_AUTO_CREATE
        )
        uploadServiceIsBound = context.bindService(
            Intent(context, QuestChangesUploadService::class.java),
            uploadServiceConnection, BIND_AUTO_CREATE
        )
    }

    fun unbindServices() {
        if (downloadServiceIsBound) context.unbindService(downloadServiceConnection)
        downloadServiceIsBound = false
        if (uploadServiceIsBound) context.unbindService(uploadServiceConnection)
        uploadServiceIsBound = false
    }

    /** Create a note for the given OSM Quest instead of answering it. The quest will turn
     * invisible.
     * @return true if successful
     */
    fun createNote(osmQuestId: Long, questTitle: String, text: String, imagePaths: List<String>?): Boolean {
        val q = osmQuestDB.get(osmQuestId)
        if (q?.status != QuestStatus.NEW) return false

        val createNote = CreateNote(null, text, q.center, questTitle, ElementKey(q.elementType, q.elementId), imagePaths)
        createNoteDB.add(createNote)

        /* The quests that reference the same element for which the user was not able to
           answer the question are removed because the to-be-created note blocks quest
           creation for other users, so those quests should be removed from the user's
           own display as well. As soon as the note is resolved, the quests will be re-
           created next time they are downloaded */
        removeQuestsForElement(q.elementType, q.elementId)
        return true
    }

    fun createNote(text: String, imagePaths: List<String>?, position: LatLon) {
        val createNote = CreateNote(null, text, position, null, null, imagePaths)
        createNoteDB.add(createNote)
    }

    private fun removeQuestsForElement(elementType: Element.Type, elementId: Long) {
        val questIdsForThisOsmElement = osmQuestDB.getAllIds(
            statusIn = listOf(QuestStatus.NEW),
            element = ElementKey(elementType, elementId)
        )

        osmQuestDB.deleteAllIds(questIdsForThisOsmElement)
        relay.onQuestsRemoved(questIdsForThisOsmElement, QuestGroup.OSM)

        osmElementDB.deleteUnreferenced()
        geometryDB.deleteUnreferenced()
    }

    /** Split a way for the given OSM Quest. The quest will turn invisible.
     * @return true if successful
     */
    fun splitWay(osmQuestId: Long, splits: List<SplitPolylineAtPosition>, source: String): Boolean {
        val q = osmQuestDB.get(osmQuestId)
        if (q?.status != QuestStatus.NEW) return false

        splitWayDB.put(OsmQuestSplitWay(osmQuestId, q.osmElementQuestType, q.elementId, source, splits))

        removeQuestsForElement(q.elementType, q.elementId)
        return true
    }

    /** Apply the user's answer to the given quest. (The quest will turn invisible.)
     * @return true if successful
     */
    fun solve(questId: Long, group: QuestGroup, answer: Any, source: String): Boolean {
        val success  = when(group) {
            QuestGroup.OSM ->      solveOsmQuest(questId, answer, source)
            QuestGroup.OSM_NOTE -> solveOsmNoteQuest(questId, answer as NoteAnswer)
        }
        relay.onQuestsRemoved(listOf(questId), group)

        return success
    }

    fun getLastSolvedOsmQuest(): OsmQuest? = osmQuestDB.getLastSolved()

    fun getOsmElement(quest: OsmQuest): OsmElement? =
        osmElementDB.get(quest.elementType, quest.elementId) as OsmElement?

    /** Undo changes made after answering a quest. */
    fun undo(quest: OsmQuest) {
        when(quest.status) {
            // not uploaded yet -> simply revert to NEW
            QuestStatus.ANSWERED, QuestStatus.HIDDEN -> {
                quest.undo()
                osmQuestDB.update(quest)
                // inform relay that the quest is visible again
                relay.onQuestsCreated(listOf(quest), QuestGroup.OSM)
            }
            // already uploaded! -> create change to reverse the previous change
            QuestStatus.CLOSED -> {
                quest.revert()
                osmQuestDB.update(quest)
                undoOsmQuestDB.add(UndoOsmQuest(quest))
            }
            else -> {
                throw IllegalStateException("Tried to undo a quest that hasn't been answered yet")
            }
        }
    }

    private fun solveOsmNoteQuest(questId: Long, answer: NoteAnswer): Boolean {
        val q = osmNoteQuestDB.get(questId)
        if (q == null || q.status !== QuestStatus.NEW) return false

        require(answer.text.isNotEmpty()) { "NoteQuest has been answered with an empty comment!" }

        q.solve(answer.text, answer.imagePaths)
        osmNoteQuestDB.update(q)
        return true
    }

    private fun solveOsmQuest(questId: Long, answer: Any, source: String): Boolean {
        // race condition: another thread (i.e. quest download thread) may have removed the
        // element already (#282). So in this case, just ignore
        val q = osmQuestDB.get(questId)
        if (q?.status !== QuestStatus.NEW) return false
        val element = osmElementDB.get(q.elementType, q.elementId) ?: return false

        val changes = createOsmQuestChanges(q, element, answer)
        if (changes == null) {
            // if applying the changes results in an error (=a conflict), the data the quest(ion)
            // was based on is not valid anymore -> like with other conflicts, silently drop the
            // user's change (#289) and the quest
            osmQuestDB.delete(questId)
            return false
        } else {
            require(!changes.isEmpty()) {
                "OsmQuest $questId (${q.type.javaClass.simpleName}) has been answered by the user but the changeset is empty!"
            }

            Log.d(TAG, "Solved a ${q.type.javaClass.simpleName} quest: $changes")
            q.solve(changes, source)
            osmQuestDB.update(q)
            prefs.edit().putLong(Prefs.LAST_SOLVED_QUEST_TIME, System.currentTimeMillis()).apply()
            return true
        }
    }

    private fun createOsmQuestChanges(quest: OsmQuest, element: Element, answer: Any) : StringMapChanges? {
        return try {
            val changesBuilder = StringMapChangesBuilder(element.tags)
            quest.osmElementQuestType.applyAnswerToUnsafe(answer, changesBuilder)
            changesBuilder.create()
        } catch (e: IllegalArgumentException) {
            // applying the changes results in an error (=a conflict)
            null
        }
    }

    /** Make the given quest invisible (per user interaction).  */
    fun hide(questId: Long, group: QuestGroup) {
        when (group) {
            QuestGroup.OSM -> {
                val q = osmQuestDB.get(questId)
                if (q?.status != QuestStatus.NEW) return
                q.hide()
                osmQuestDB.update(q)
                relay.onQuestsRemoved(listOf(q.id!!), group)
            }
            QuestGroup.OSM_NOTE -> {
                val q = osmNoteQuestDB.get(questId)
                if (q?.status != QuestStatus.NEW) return
                q.hide()
                osmNoteQuestDB.update(q)
                relay.onQuestsRemoved(listOf(q.id!!), group)
            }
        }
    }

    /** Retrieve the given quest from local database  */
    fun get(questId: Long, group: QuestGroup): Quest? = when (group) {
        QuestGroup.OSM -> osmQuestDB.get(questId)
        QuestGroup.OSM_NOTE -> osmNoteQuestDB.get(questId)
    }

    /** Retrieve all visible (=new) quests in the given bounding box from local database
     * asynchronously.  */
    suspend fun retrieve(bbox: BoundingBox) {
        withContext(Dispatchers.IO) {
            val osmQuests = osmQuestDB.getAll(
                statusIn = listOf(QuestStatus.NEW),
                bounds = bbox,
                questTypes = questTypeNames
            )
            if (osmQuests.isNotEmpty()) {
                relay.onQuestsCreated(osmQuests, QuestGroup.OSM)
            }

            val osmNoteQuests = osmNoteQuestDB.getAll(
                statusIn = listOf(QuestStatus.NEW),
                bounds = bbox)
            if (osmNoteQuests.isNotEmpty()) {
                relay.onQuestsCreated(osmNoteQuests, QuestGroup.OSM_NOTE)
            }
        }
    }

    /** Download quests in at least the given bounding box asynchronously. The next-bigger rectangle
     * in a (z14) tiles grid that encloses the given bounding box will be downloaded.
     *
     * @param bbox the minimum area to download
     * @param maxQuestTypesToDownload download at most the given number of quest types. null for
     * unlimited
     * @param isPriority whether this shall be a priority download (cancels previous downloads and
     * puts itself in the front)
     */
    fun download(bbox: BoundingBox, maxQuestTypesToDownload: Int? = null, isPriority: Boolean = false) {
        val tilesRect = SlippyMapMath.enclosingTiles(bbox, ApplicationConstants.QUEST_TILE_ZOOM)
        context.startService(QuestDownloadService.createIntent(context, tilesRect, maxQuestTypesToDownload, isPriority))
    }

    /** Collect and upload all changes made by the user  */
    fun upload() {
        context.startService(QuestChangesUploadService.createIntent(context))
    }

    /** Delete old unsolved quests */
    suspend fun deleteOld() {
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis() - ApplicationConstants.DELETE_UNSOLVED_QUESTS_AFTER
            var deleted = osmQuestDB.deleteAll(
                statusIn = listOf(QuestStatus.NEW, QuestStatus.HIDDEN),
                changedBefore = timestamp
            )
            deleted += osmNoteQuestDB.deleteAll(
                statusIn = listOf(QuestStatus.NEW, QuestStatus.HIDDEN),
                changedBefore = timestamp
            )

            if (deleted > 0) {
                Log.d(TAG, "Deleted $deleted old unsolved quests")

                osmElementDB.deleteUnreferenced()
                geometryDB.deleteUnreferenced()
            }
        }
    }

    fun addListener(listener: VisibleQuestListener) {
        listeners.add(listener)
    }
    fun removeListener(listener: VisibleQuestListener) {
        listeners.remove(listener)
    }

    companion object {
        private const val TAG = "QuestController"
    }
}
