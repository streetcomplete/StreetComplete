package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.streetcomplete.data.osm.delete_element.DeleteOsmElementDao
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.osmquest.undo.UndoOsmQuestDao
import de.westnordost.streetcomplete.data.osm.splitway.OsmQuestSplitWayDao
import javax.inject.Inject

/** Supplies a set of elements for which no quests should be created */
class BlacklistedElementsSource @Inject constructor(
    private val splitWayDao: OsmQuestSplitWayDao,
    private val deleteOsmElementDao: DeleteOsmElementDao,
    private val undoOsmQuestDao: UndoOsmQuestDao
) {
    fun getAll(): List<ElementKey> = (
            splitWayDao.getAll() +
            deleteOsmElementDao.getAll() +
            undoOsmQuestDao.getAll()
        ).map { ElementKey(it.elementType, it.elementId) }
}
