package de.westnordost.streetcomplete.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable

@Entity(tableName = NodeTable.NAME)
data class Node(
    @PrimaryKey
    val id: Int,
    var version: Int,
    var latitude: Double,
    var longitude: Double,
    val tags: Map<String, String>?
)
