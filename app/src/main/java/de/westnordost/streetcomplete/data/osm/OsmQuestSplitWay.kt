package de.westnordost.streetcomplete.data.osm

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.changes.SplitPolylineAtPosition
import de.westnordost.streetcomplete.data.osm.upload.UploadableInChangeset

data class OsmQuestSplitWay(
    val questId: Long,
    val questType: OsmElementQuestType<*>,
    val wayId: Long,
    override val source: String,
    val splits: List<SplitPolylineAtPosition>) : UploadableInChangeset {

    override val osmElementQuestType get() = questType
    override val elementType get() = Element.Type.WAY
    override val elementId get() = wayId
}
