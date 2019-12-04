package de.westnordost.streetcomplete.data.osm.upload

import androidx.annotation.CallSuper
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.QuestGroup
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.VisibleQuestListener
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.OsmQuest
import de.westnordost.streetcomplete.data.osm.OsmQuestGiver
import de.westnordost.streetcomplete.data.osm.download.OsmApiElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.upload.Uploader

import java.util.concurrent.atomic.AtomicBoolean

abstract class OsmInChangesetsUploader<T : UploadableInChangeset>(
    private val elementDB: MergedElementDao,
    private val elementGeometryDB: ElementGeometryDao,
    private val changesetManager: OpenQuestChangesetsManager,
    private val questGiver: OsmQuestGiver,
    private val statisticsDB: QuestStatisticsDao,
    private val osmApiElementGeometryCreator: OsmApiElementGeometryCreator
    ): Uploader {

    override var visibleQuestListener: VisibleQuestListener? = null
    override var uploadedChangeListener: OnUploadedChangeListener? = null

    @Synchronized @CallSuper override fun upload(cancelled: AtomicBoolean) {
        if (cancelled.get()) return

        val uploadedQuestTypes = mutableSetOf<OsmElementQuestType<*>>()
        val createdOsmQuests = mutableListOf<OsmQuest>()
        val removedOsmQuestIds = mutableListOf<Long>()

        for (quest in getAll()) {
            if (cancelled.get()) break

            try {
                val uploadedElements = uploadSingle(quest)
                for (element in uploadedElements) {
                    val questUpdates = updateElement(element)
                    createdOsmQuests.addAll(questUpdates.createdQuests)
                    removedOsmQuestIds.addAll(questUpdates.removedQuestIds)
                }
                uploadedQuestTypes.add(quest.osmElementQuestType)
                onUploadSuccessful(quest)
                uploadedChangeListener?.onUploaded()
                statisticsDB.addOne(quest.osmElementQuestType.name)
            } catch (e: ElementIncompatibleException) {
                val questIds = deleteElement(quest.elementType, quest.elementId)
                removedOsmQuestIds.addAll(questIds)
                onUploadFailed(quest, e)
                uploadedChangeListener?.onDiscarded()
            } catch (e: ElementConflictException) {
                onUploadFailed(quest, e)
                uploadedChangeListener?.onDiscarded()
            }
        }
        cleanUp(uploadedQuestTypes)

        if (createdOsmQuests.isNotEmpty()) {
            visibleQuestListener?.onQuestsCreated(createdOsmQuests, QuestGroup.OSM)
        }
        if (removedOsmQuestIds.isNotEmpty()) {
            visibleQuestListener?.onQuestsRemoved(removedOsmQuestIds, QuestGroup.OSM)
        }
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

    private fun updateElement(newElement: Element): OsmQuestGiver.QuestUpdates {
        val geometry = osmApiElementGeometryCreator.create(newElement)
        if (geometry != null) {
            elementGeometryDB.put(ElementGeometryEntry(newElement.type, newElement.id, geometry))
            elementDB.put(newElement)
            return questGiver.updateQuests(newElement)
        } else {
            // new element has invalid geometry
            return OsmQuestGiver.QuestUpdates(listOf(), deleteElement(newElement.type, newElement.id))
        }
    }

    private fun deleteElement(elementType: Element.Type, elementId: Long): List<Long> {
        elementDB.delete(elementType, elementId)
        elementGeometryDB.delete(elementType, elementId)
        return questGiver.deleteQuests(elementType, elementId)
    }

    @CallSuper protected open fun cleanUp(questTypes: Set<OsmElementQuestType<*>>) {
        elementGeometryDB.deleteUnreferenced()
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
