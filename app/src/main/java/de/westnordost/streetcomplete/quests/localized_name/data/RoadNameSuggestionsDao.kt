package de.westnordost.streetcomplete.quests.localized_name.data

import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

import javax.inject.Inject

import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.ktx.getBlob
import de.westnordost.streetcomplete.ktx.query
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.GEOMETRY
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.MAX_LATITUDE
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.MAX_LONGITUDE
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.MIN_LATITUDE
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.MIN_LONGITUDE
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.NAMES
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.WAY_ID
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.NAME
import de.westnordost.streetcomplete.util.Serializer
import de.westnordost.streetcomplete.ktx.toObject
import de.westnordost.streetcomplete.util.enclosingBoundingBox
import de.westnordost.streetcomplete.util.isWithinDistanceOf

class RoadNameSuggestionsDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val serializer: Serializer
) {
    private val db get() = dbHelper.writableDatabase

    fun putRoad(wayId: Long, namesByLanguage: Map<String, String>, geometry: List<LatLon>) {
        val bbox = geometry.enclosingBoundingBox()
        val v = contentValuesOf(
            WAY_ID to wayId,
            NAMES to serializer.toBytes(HashMap(namesByLanguage)),
            GEOMETRY to serializer.toBytes(ArrayList(geometry)),
            MIN_LATITUDE to bbox.minLatitude,
            MIN_LONGITUDE to bbox.minLongitude,
            MAX_LATITUDE to bbox.maxLatitude,
            MAX_LONGITUDE to bbox.maxLongitude
        )
        db.replaceOrThrow(NAME, null, v)
    }

    /** returns something like [{"": "17th Street", "de": "17. Straße", "en": "17th Street" }, ...] */
    fun getNames(points: List<LatLon>, maxDistance: Double): List<MutableMap<String, String>> {

        // preselection via intersection check of bounding boxes
        val query = Array(points.size) {
            "$MIN_LATITUDE <= ? AND $MIN_LONGITUDE <= ? AND " +
            "$MAX_LATITUDE >= ? AND $MAX_LONGITUDE >= ? "
        }.joinToString(" OR ")

        val args = arrayOfNulls<String>(points.size * 4)
        for (i in points.indices) {
            val point = points[i]
            val bbox = point.enclosingBoundingBox(maxDistance)
            val ai = i * 4
            args[ai + 0] = "" + bbox.maxLatitude
            args[ai + 1] = "" + bbox.maxLongitude
            args[ai + 2] = "" + bbox.minLatitude
            args[ai + 3] = "" + bbox.minLongitude
        }

        val cols = arrayOf(GEOMETRY, NAMES)

        val result = mutableListOf<MutableMap<String, String>>()

        db.query(NAME, cols, query, args.requireNoNulls()) { cursor ->
            val geometry: ArrayList<LatLon> = serializer.toObject(cursor.getBlob(GEOMETRY))
            if (points.isWithinDistanceOf(maxDistance, geometry)) {
                val map: HashMap<String,String> = serializer.toObject(cursor.getBlob(NAMES))
                result.add(map)
            }
        }
        return result
    }
}
