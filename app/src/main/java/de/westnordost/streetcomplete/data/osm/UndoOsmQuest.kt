package de.westnordost.streetcomplete.data.osm

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.upload.HasElementTagChanges
import de.westnordost.streetcomplete.data.osm.upload.UploadableInChangeset

class UndoOsmQuest(
    val id: Long?,
    val type: OsmElementQuestType<*>,
    override val elementType: Element.Type,
    override val elementId: Long,
    override val changes: StringMapChanges,
    val changesSource: String,
    val geometry: ElementGeometry
) : UploadableInChangeset, HasElementTagChanges {

    constructor(quest: OsmQuest) : this(
        null, quest.osmElementQuestType, quest.elementType, quest.elementId,
        quest.changes!!.reversed(), quest.changesSource!!, quest.geometry)

    /* can't ask the quest here if it is applicable to the element or not, because the change
       of the revert is exactly the opposite of what the quest would normally change and the
       element ergo has the changes already applied that a normal quest would add */
    override fun isApplicableTo(element: Element) = true

    override val source get() = changesSource
    override val osmElementQuestType  get() = type
}
