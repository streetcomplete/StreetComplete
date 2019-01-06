package de.westnordost.streetcomplete.quests.localized_name.data

import android.database.sqlite.SQLiteOpenHelper

import java.util.ArrayList
import java.util.HashMap

import javax.inject.Inject

import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.GEOMETRY
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.MAX_LATITUDE
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.MAX_LONGITUDE
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.MIN_LATITUDE
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.MIN_LONGITUDE
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.NAMES
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.WAY_ID
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.NAME
import de.westnordost.streetcomplete.util.Serializer
import de.westnordost.streetcomplete.util.SphericalEarthMath
import de.westnordost.streetcomplete.ktx.toObject
import de.westnordost.streetcomplete.ktx.transaction

// TODO only open in order to be able to mock it in tests
open class RoadNameSuggestionsDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val serializer: Serializer
) {

    private val insert = dbHelper.writableDatabase.compileStatement(
        "INSERT OR REPLACE INTO $NAME " +
        "($WAY_ID,$NAMES,$GEOMETRY,$MIN_LATITUDE,$MIN_LONGITUDE,$MAX_LATITUDE,$MAX_LONGITUDE) " +
        "values (?,?,?,?,?,?,?);"
    )

    fun putRoad(wayId: Long, namesByLanguage: Map<String, String>, geometry: List<LatLon>) {
        val bbox = SphericalEarthMath.enclosingBoundingBox(geometry)

        dbHelper.writableDatabase.transaction {
            insert.bindLong(1, wayId)
            insert.bindBlob(2, serializer.toBytes(HashMap(namesByLanguage)))
            insert.bindBlob(3, serializer.toBytes(ArrayList(geometry)))
            insert.bindDouble(4, bbox.minLatitude)
            insert.bindDouble(5, bbox.minLongitude)
            insert.bindDouble(6, bbox.maxLatitude)
            insert.bindDouble(7, bbox.maxLongitude)

            insert.executeInsert()
            insert.clearBindings()
        }
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
            val bbox = SphericalEarthMath.enclosingBoundingBox(point, maxDistance)
            val ai = i * 4
            args[ai + 0] = "" + bbox.maxLatitude
            args[ai + 1] = "" + bbox.maxLongitude
            args[ai + 2] = "" + bbox.minLatitude
            args[ai + 3] = "" + bbox.minLongitude
        }

        val cols = arrayOf(GEOMETRY, NAMES)

        val result = mutableListOf<MutableMap<String, String>>()

        dbHelper.readableDatabase.query(NAME, cols, query, args, null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast) {
                    val geometry: ArrayList<LatLon> = serializer.toObject(cursor.getBlob(0))
                    if (SphericalEarthMath.isWithinDistance(maxDistance, points, geometry)) {
                        val map: HashMap<String,String> = serializer.toObject(cursor.getBlob(1))
                        result.add(map)
                    }
                    cursor.moveToNext()
                }
            }
        }
        return result
    }
}
