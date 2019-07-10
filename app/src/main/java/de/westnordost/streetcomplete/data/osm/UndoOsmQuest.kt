package de.westnordost.streetcomplete.data.osm

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import java.util.*

// TODO it is a little dirty that this inherits from OsmQuest. Better would be a common interface
class UndoOsmQuest(
    id: Long?, type: OsmElementQuestType<*>, elementType: Element.Type, elementId: Long,
    changes: StringMapChanges?, changesSource: String?, lastUpdate: Date?, geometry: ElementGeometry
) : OsmQuest(id, type, elementType, elementId, QuestStatus.ANSWERED, changes, changesSource,
    lastUpdate, geometry) {


    override fun isApplicableTo(element: Element): Boolean? {
        // can't ask the quest here if it is applicable to the element or not, because the change
        // of the revert is exactly the opposite of what the quest would normally change and the
        // element ergo has the changes already applied that a normal quest would add
        return true
    }
}
