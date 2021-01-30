package de.westnordost.streetcomplete.data.osm.upload

import androidx.annotation.CallSuper
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.OsmElementController
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.upload.Uploader

import java.util.concurrent.atomic.AtomicBoolean

/** Base class for all uploaders which upload OSM data. They all have in common that they handle
 *  OSM data (of course), and that the data is uploaded in changesets. */
abstract class OsmInChangesetsUploader<T : UploadableInChangeset>(
    private val changesetManager: OpenQuestChangesetsManager,
    private val osmElementController: OsmElementController
): Uploader {

    override var uploadedChangeListener: OnUploadedChangeListener? = null

    @Synchronized @CallSuper override fun upload(cancelled: AtomicBoolean) {
        if (cancelled.get()) return

        val uploadedQuestTypes = mutableSetOf<OsmElementQuestType<*>>()

        for (quest in getAll()) {
            if (cancelled.get()) break

            try {
                val uploadedElements = uploadSingle(quest)
                /* onUploadSuccessful must be called before updating the element because in there,
                   the entry from the database that this-thing-needs-to-be-uploaded is removed.
                   Such entry blocks the creation of new quests. See #2418
                */
                onUploadSuccessful(quest)
                updateElements(uploadedElements, quest)
                uploadedQuestTypes.add(quest.osmElementQuestType)
                uploadedChangeListener?.onUploaded(quest.osmElementQuestType.name, quest.position)
            } catch (e: ElementIncompatibleException) {
                osmElementController.deleteAll(listOf(ElementKey(quest.elementType, quest.elementId)))
                onUploadFailed(quest, e)
                uploadedChangeListener?.onDiscarded(quest.osmElementQuestType.name, quest.position)
            } catch (e: ElementConflictException) {
                onUploadFailed(quest, e)
                uploadedChangeListener?.onDiscarded(quest.osmElementQuestType.name, quest.position)
            }
        }
    }

    protected open fun updateElements(elements: List<Element>, quest: T) {
        osmElementController.putAll(elements)
    }

    private fun uploadSingle(quest: T) : List<Element> {
        val element = osmElementController.get(quest.elementType, quest.elementId)
            ?: throw ElementDeletedException("Element deleted")

        return try {
            val changesetId = changesetManager.getOrCreateChangeset(quest.osmElementQuestType, quest.source)
            uploadSingle(changesetId, quest, element)
        } catch (e: ChangesetConflictException) {
            val changesetId = changesetManager.createChangeset(quest.osmElementQuestType, quest.source)
            uploadSingle(changesetId, quest, element)
        }
    }

    protected abstract fun getAll() : Collection<T>
    /** Upload the changes for a single quest and element.
     *  Returns the updated element(s) for which it should be checked whether they are eligible
     *  for new quests (or not eligible anymore for existing quests) */
    protected abstract fun uploadSingle(changesetId: Long, quest: T, element: Element): List<Element>

    protected abstract fun onUploadSuccessful(quest: T)
    protected abstract fun onUploadFailed(quest: T, e: Throwable)
}

private val QuestType<*>.name get() = javaClass.simpleName
