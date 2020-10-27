package de.westnordost.streetcomplete.data

import androidx.room.TypeConverter
import de.westnordost.streetcomplete.data.model.LatLon
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.quest.QuestStatus
import de.westnordost.streetcomplete.ktx.toObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object Converters {
    private val serializer = DbModule.serializer()

    @JvmStatic
    @TypeConverter
    fun dateToTimestamp(date: Date) = date.time

    @JvmStatic
    @TypeConverter
    fun timestampToDate(timestamp: Long) = Date(timestamp)

    @JvmStatic
    @TypeConverter
    fun polylinesToBlob(polylines: List<List<LatLon>>) = serializer.toBytes(ArrayList(polylines))

    @JvmStatic
    @TypeConverter
    fun blobToPolylines(blob: ByteArray): List<List<LatLon>> = serializer.toObject<ArrayList<ArrayList<LatLon>>>(blob)

    @JvmStatic
    @TypeConverter
    fun questTypeToString(questStatus: QuestStatus) = questStatus.name

    @JvmStatic
    @TypeConverter
    fun stringToQuestType(string: String) = QuestStatus.valueOf(string)

    @JvmStatic
    @TypeConverter
    fun stringMapChangesToBlob(stringMapChanges: StringMapChanges?) = stringMapChanges?.let { serializer.toBytes(it) }

    @JvmStatic
    @TypeConverter
    fun blobToStringMapChanges(blob: ByteArray?) = blob?.let { serializer.toObject<StringMapChanges>(it) }

    @JvmStatic
    @TypeConverter
    fun tagsToBlob(tags: Map<String, String>?) = tags?.let { serializer.toBytes(HashMap(it)) }

    @JvmStatic
    @TypeConverter
    fun blobToTags(blob: ByteArray?): Map<String, String>? = blob?.let { serializer.toObject<HashMap<String, String>>(it) }

    @JvmStatic
    @TypeConverter
    fun nodeIdsToBlob(nodeIds: List<Long>) = serializer.toBytes(ArrayList(nodeIds))

    @JvmStatic
    @TypeConverter
    fun blobToNodeIds(blob: ByteArray): List<Long> = serializer.toObject<ArrayList<Long>>(blob)
}
