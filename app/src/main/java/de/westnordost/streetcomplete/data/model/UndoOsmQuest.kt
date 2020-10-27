package de.westnordost.streetcomplete.data.model

import androidx.room.*
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryTable
import de.westnordost.streetcomplete.data.osm.osmquest.undo.UndoOsmQuestTable

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ElementGeometry::class,
            parentColumns = [ElementGeometryTable.Columns.ELEMENT_ID, ElementGeometryTable.Columns.ELEMENT_TYPE],
            childColumns = [UndoOsmQuestTable.Columns.ELEMENT_ID, UndoOsmQuestTable.Columns.ELEMENT_TYPE]
        )
    ],
    indices = [
        Index(value = [
            UndoOsmQuestTable.Columns.QUEST_TYPE, UndoOsmQuestTable.Columns.ELEMENT_ID,
            UndoOsmQuestTable.Columns.ELEMENT_TYPE
        ], unique = true)
    ],
    tableName = UndoOsmQuestTable.NAME
)
data class UndoOsmQuest(
    @PrimaryKey
    @ColumnInfo(name = UndoOsmQuestTable.Columns.QUEST_ID)
    val id: Int,

    @ColumnInfo(name = UndoOsmQuestTable.Columns.QUEST_TYPE)
    var type: String,

    @ColumnInfo(name = UndoOsmQuestTable.Columns.TAG_CHANGES)
    var changes: StringMapChanges,

    @ColumnInfo(name = UndoOsmQuestTable.Columns.CHANGES_SOURCE)
    var changesSource: String,

    @ColumnInfo(name = UndoOsmQuestTable.Columns.ELEMENT_ID)
    var elementId: Int,

    @ColumnInfo(name = UndoOsmQuestTable.Columns.ELEMENT_TYPE)
    var elementType: String
)
