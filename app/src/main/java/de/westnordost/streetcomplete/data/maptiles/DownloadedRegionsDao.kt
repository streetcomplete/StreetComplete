package de.westnordost.streetcomplete.data.maptiles

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.maptiles.DownloadedRegionsTable.Columns.DATE
import de.westnordost.streetcomplete.data.maptiles.DownloadedRegionsTable.Columns.ID
import de.westnordost.streetcomplete.data.maptiles.DownloadedRegionsTable.NAME
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import org.maplibre.android.offline.OfflineRegion

// todo: dummy functions, create the table later
class DownloadedRegionsDao(private val db: Database) {
    fun put(offlineRegion: OfflineRegion) {
//        db.insert(
//            NAME,
//            listOf(ID to offlineRegion.id, DATE to nowAsEpochMilliseconds())
//        )
    }

    fun clear() {
//        db.delete(NAME)
    }

    fun deleteOlderThan(time: Long) {
//        db.delete(NAME, where = "$DATE < $time")
    }

    fun delete(id: Long) {
//        db.delete(NAME, where = "$ID = $id")
    }

    fun getIdsOlderThan(time: Long): Collection<Long> {
//        return db.query(NAME,
//            columns = arrayOf(ID),
//            where = "$DATE < $time"
//        ) { it.getLong(ID) }
        return emptyList() // dummy
    }
}
