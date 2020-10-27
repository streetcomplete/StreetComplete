package de.westnordost.streetcomplete.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.westnordost.streetcomplete.data.osm.mapdata.WayTable

@Entity(tableName = WayTable.NAME)
data class Way(
    @PrimaryKey
    val id: Int,
    var version: Int,
    val tags: Map<String, String>?,

    @ColumnInfo(name = WayTable.Columns.NODE_IDS)
    val nodeIds: List<Long>
)
