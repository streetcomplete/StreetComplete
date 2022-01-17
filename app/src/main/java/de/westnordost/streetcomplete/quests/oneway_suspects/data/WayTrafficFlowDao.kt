package de.westnordost.streetcomplete.quests.oneway_suspects.data

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowTable.Columns.IS_FORWARD
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowTable.Columns.WAY_ID
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowTable.NAME
import javax.inject.Inject

class WayTrafficFlowDao @Inject constructor(private val db: Database) {

    fun put(wayId: Long, isForward: Boolean) {
        db.replace(NAME, listOf(
            WAY_ID to wayId,
            IS_FORWARD to if (isForward) 1 else 0
        ))
    }

    /** returns whether the direction of road user flow is forward or null if unknown */
    fun isForward(wayId: Long): Boolean? =
        db.queryOne(NAME,
            columns = arrayOf(IS_FORWARD),
            where = "$WAY_ID = $wayId"
        ) { it.getInt(IS_FORWARD) != 0 }

    fun delete(wayId: Long) {
        db.delete(NAME, "$WAY_ID = $wayId")
    }

    fun deleteUnreferenced() {
        db.delete(NAME, "$WAY_ID NOT IN (SELECT ${WayTables.Columns.ID} AS $WAY_ID FROM ${WayTables.NAME});")
    }
}
