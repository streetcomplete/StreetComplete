package de.westnordost.streetcomplete.data.othersource

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
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
    single { OtherSourceQuestController(get(named("CountryBoundariesFuture")), get(), get()) }
    single { OsmoseDao(get(), get()) }
}

// todo: for a start, add external and osmose quests here!
//  external: allow specifying coordinates instead of element, though not sure what to do them
//   just show message, and maybe allow creating a node with pre-defined tags?

// currently does more than the others, maybe split later
class OtherSourceQuestController(
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>,
    questTypeRegistry: QuestTypeRegistry,
    private val osmoseDao: OsmoseDao,
) : ElementEditsSource.Listener {
    interface Listener {
        fun onUpdated(addedQuests: Collection<OtherSourceQuest> = emptyList(), deletedQuestKeys: Collection<OtherSourceQuestKey> = emptyList())
        fun onInvalidate()
    }
    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()
    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private val questTypes = questTypeRegistry.filterIsInstance<OtherSourceQuestType>()

    fun delete(key: OtherSourceQuestKey) {
        if (key.source == osmoseDao.type.source && osmoseDao.delete(key.id)) {
            listeners.forEach { it.onUpdated(deletedQuestKeys = listOf(key)) }
        }
    }

    fun getAllVisibleInBBox(bbox: BoundingBox, visibleQuestTypeNames: List<String>? = null): List<OtherSourceQuest> {
        val quests = if (visibleQuestTypeNames == null || OsmoseQuest::class.simpleName in visibleQuestTypeNames)
                osmoseDao.getAllQuests(bbox)
            else emptyList()
        return quests
        // todo: once hiding is done
//        val hiddenKeys = getAllHidden().toHashSet()
//        return quests.filterNot { it.key in hiddenKeys }
    }

    fun get(key: OtherSourceQuestKey): OtherSourceQuest? =
        if (key.source == osmoseDao.type.source)
            osmoseDao.getQuest(key.id)
        else null

    // each dao / controller is responsible for persisting data and then creating quests
    suspend fun download(bbox: BoundingBox) {
        withContext(Dispatchers.IO) {
            val countryBoundaries = countryBoundariesFuture.get()
            val obsoleteQuestKeys = mutableListOf<OtherSourceQuestKey>()
            val newQuests = mutableListOf<OtherSourceQuest>()

            questTypes.forEach { type -> // todo: maybe do parallel once we have more sources
                if (!countryBoundaries.intersects(bbox, type.enabledInCountries)) return@forEach
                val previousQuests = type.getQuests(bbox).map { it.key }
                val quests = type.download(bbox)
                newQuests.addAll(quests)
                val questKeys = HashSet<OtherSourceQuestKey>(quests.size).apply { quests.forEach { add(it.key) } }
                obsoleteQuestKeys.addAll(previousQuests.filterNot { it in questKeys })
            }
            listeners.forEach { it.onUpdated(newQuests, obsoleteQuestKeys) }
        }
    }

    fun upload() {
        questTypes.forEach { // todo: maybe do parallel once we have more sources
            it.upload()
        }
    }

    fun invalidate() = listeners.forEach { it.onInvalidate() }


    // hiding / unhiding... todo: do it later
    // this is also used so pins actually disappear when quest is solved
    fun tempHide(key: OtherSourceQuestKey) {
        listeners.forEach { it.onUpdated(deletedQuestKeys = listOf(key)) }
    }

/*
    // todo: simple table with id, source, timestamp
    //  check hidden when getAllVisibleInBBox
    //  add the class
    //  dao with functionality similar to noteQuestsHiddenDao
    interface HideOtherSourceQuestListener {
        fun onHid(edit: OtherSourceQuestHidden)
        fun onUnhid(edit: OtherSourceQuestHidden)
        fun onUnhidAll()
    }
    private val hideListeners: MutableList<HideOtherSourceQuestListener> = CopyOnWriteArrayList()

    fun hide(key: OtherSourceQuestKey) {

    }

    fun unhide(key: OtherSourceQuestKey) {

    }

    fun unhideAll() {

    }
*/
    override fun onAddedEdit(edit: ElementEdit) {}

    override fun onSyncedEdit(edit: ElementEdit) {
//        getQuestKey(edit.id)...
    }

    // for undoing stuff
    override fun onDeletedEdits(edits: List<ElementEdit>) {
        edits.forEach {
            if (it.type is OsmoseQuest) {
                // how to identify? id / quest key is not stored in edit
                // do i really need a separate edit type, even if it's an elementEdit?
                // for now do some crude workaround that should be find most of the time
                osmoseDao.setFromDoneToNotAnsweredNear(it.position)
            }
        }
    }
}

/* todo: is this necessary for anything? -> yes, for proper undo...
    but then it need to duplicate the whole elementEdits db -> not worth it currently
    and maybe also necessary for new edit types
data class OtherSourceEdit(
    override val position: LatLon,
    override val isSynced: Boolean?,
    val action: Unit, // depending on the source and what was done, need some type...
                      //  osmose may edit several elements, or maybe delete a node... how to do? simply allow a list of elementEdits?
) : Edit {
    override val key: OtherSourceEditKey // add some key
        get() = TODO("Not yet implemented")
    override val createdTimestamp: Long
        get() = TODO("Not yet implemented")
    override val isUndoable: Boolean
        get() = TODO("Not yet implemented")

}

class OtherSourceEditKey(val source: String, val id: Long) : EditKey() // better have a key for each source instead of this
*/
data class OtherSourceQuest(
    val id: String, // string because e.g. osmose uses uuid and not number
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
    override val title: Int get() = getTitle(emptyMap())
    fun getTitle(tags: Map<String, String>): Int
    fun getTitleArgs(tags: Map<String, String>): Array<String> = arrayOf()
    // how to do it with the answer? might edit tags on an element, might create node, might not interact with osm at all...
    val source: String
    fun download(bbox: BoundingBox): Collection<OtherSourceQuest> // should download and return all quests in the downloaded area
    fun upload() // simply upload...
    fun getQuests(bbox: BoundingBox): Collection<OtherSourceQuest>
    val highlightedElementsRadius: Double? get() = null
    fun getHighlightedElements(getMapData: () -> MapDataWithGeometry): Sequence<Element> = emptySequence()

    // necessary to clean old data, will be called with (nearly) current time for clearing the stored data
    override fun deleteMetadataOlderThan(timestamp: Long)

    val enabledInCountries: Countries get() = AllCountries
}
