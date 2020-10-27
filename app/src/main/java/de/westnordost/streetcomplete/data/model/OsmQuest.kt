package de.westnordost.streetcomplete.data.model

import androidx.room.*
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryTable
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestTable
import de.westnordost.streetcomplete.data.quest.QuestStatus
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ElementGeometry::class,
            parentColumns = [ElementGeometryTable.Columns.ELEMENT_ID, ElementGeometryTable.Columns.ELEMENT_TYPE],
            childColumns = [OsmQuestTable.Columns.ELEMENT_ID, OsmQuestTable.Columns.ELEMENT_TYPE]
        )
    ],
    indices = [
        Index(value = [
            OsmQuestTable.Columns.QUEST_TYPE, OsmQuestTable.Columns.ELEMENT_ID, OsmQuestTable.Columns.ELEMENT_TYPE
        ], unique = true)
    ],
    tableName = OsmQuestTable.NAME
)
data class OsmQuest(
    @PrimaryKey
    @ColumnInfo(name = OsmQuestTable.Columns.QUEST_ID)
    val id: Int,

    @ColumnInfo(name = OsmQuestTable.Columns.QUEST_TYPE)
    var type: String,

    @ColumnInfo(name = OsmQuestTable.Columns.QUEST_STATUS)
    var status: QuestStatus,

    @ColumnInfo(name = OsmQuestTable.Columns.TAG_CHANGES)
    var changes: StringMapChanges?,

    @ColumnInfo(name = OsmQuestTable.Columns.CHANGES_SOURCE)
    var changesSource: String?,

    @ColumnInfo(name = OsmQuestTable.Columns.LAST_UPDATE)
    var lastUpdate: Date,

    @ColumnInfo(name = OsmQuestTable.Columns.ELEMENT_ID)
    var elementId: Int,

    @ColumnInfo(name = OsmQuestTable.Columns.ELEMENT_TYPE)
    var elementType: String
)
