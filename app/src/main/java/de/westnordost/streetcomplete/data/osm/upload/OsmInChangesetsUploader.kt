package de.westnordost.streetcomplete.data.osm.upload

import androidx.annotation.CallSuper
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestGiver
import de.westnordost.streetcomplete.data.osm.elementgeometry.OsmApiElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.mapdata.MergedElementDao
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.upload.Uploader

import java.util.concurrent.atomic.AtomicBoolean

/** Base class for all uploaders which upload OSM data. They all have in common that they handle
 *  OSM data (of course), and that the data is uploaded in changesets. */
abstract class OsmInChangesetsUploader<T : UploadableInChangeset>(
        private val elementDB: MergedElementDao,
        private val elementGeometryDB: ElementGeometryDao,
        private val changesetManager: OpenQuestChangesetsManager,
        private val questGiver: OsmQuestGiver,
        private val osmApiElementGeometryCreator: OsmApiElementGeometryCreator
    ): Uploader {

    override var uploadedChangeListener: OnUploadedChangeListener? = null

    @Synchronized @CallSuper override fun upload(cancelled: AtomicBoolean) {
        if (cancelled.get()) return

        val uploadedQuestTypes = mutableSetOf<OsmElementQuestType<*>>()

        for (quest in getAll()) {
            if (cancelled.get()) break

            try {
                val uploadedElements = uploadSingle(quest)
                for (element in uploadedElements) {
                    updateElement(element)
                }
                uploadedQuestTypes.add(quest.osmElementQuestType)
                onUploadSuccessful(quest)
                uploadedChangeListener?.onUploaded(quest.osmElementQuestType.name, quest.position)
            } catch (e: ElementIncompatibleException) {
                deleteElement(quest.elementType, quest.elementId)
                onUploadFailed(quest, e)
                uploadedChangeListener?.onDiscarded(quest.osmElementQuestType.name, quest.position)
            } catch (e: ElementConflictException) {
                onUploadFailed(quest, e)
                uploadedChangeListener?.onDiscarded(quest.osmElementQuestType.name, quest.position)
            }
        }
        cleanUp(uploadedQuestTypes)
    }

    private fun uploadSingle(quest: T) : List<Element> {
        val element = elementDB.get(quest.elementType, quest.elementId)
            ?: throw ElementDeletedException("Element deleted")

        return try {
            val changesetId = changesetManager.getOrCreateChangeset(quest.osmElementQuestType, quest.source)
            uploadSingle(changesetId, quest, element)
        } catch (e: ChangesetConflictException) {
            val changesetId = changesetManager.createChangeset(quest.osmElementQuestType, quest.source)
            uploadSingle(changesetId, quest, element)
        }
    }

    /* TODO REFACTOR: It shouldn't be the duty of OsmInChangesetsUploader to do (or delegate) the
     *                work necessary that entails when updating an element (grant/remove quests,
     *                update element geometry). Instead, there should be an observer on the
     *                ElementDao (or a controller in front of it) that takes care of that.
     *
     * This will remove the dependencies to  elementGeometryDB, questGiver etc */
    private fun updateElement(newElement: Element) {
        val geometry = osmApiElementGeometryCreator.create(newElement)
        if (geometry != null) {
            elementGeometryDB.put(ElementGeometryEntry(newElement.type, newElement.id, geometry))
            elementDB.put(newElement)
            questGiver.updateQuests(newElement, geometry)
        } else {
            // new element has invalid geometry
            deleteElement(newElement.type, newElement.id)
        }
    }

    private fun deleteElement(elementType: Element.Type, elementId: Long) {
        elementDB.delete(elementType, elementId)
        questGiver.deleteQuests(elementType, elementId)
    }

    @CallSuper protected open fun cleanUp(questTypes: Set<OsmElementQuestType<*>>) {
        elementDB.deleteUnreferenced()
        // must be after unreferenced elements have been deleted
        for (questType in questTypes) {
            questType.cleanMetadata()
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
