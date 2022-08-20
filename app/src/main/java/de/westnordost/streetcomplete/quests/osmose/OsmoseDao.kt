package de.westnordost.streetcomplete.quests.osmose

import android.content.SharedPreferences
import android.util.Log
import de.westnordost.streetcomplete.ApplicationConstants.USER_AGENT
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.FALSE_POSITIVE
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.ITEM
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.SUBTITLE
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.TITLE
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.Columns.UUID
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable.NAME
import de.westnordost.streetcomplete.quests.questPrefix
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class OsmoseDao(
    private val db: Database,
    private val sharedPrefs: SharedPreferences,
) {
    val client = OkHttpClient()

    fun download(bbox: BoundingBox) {
        if (!sharedPrefs.getBoolean(questPrefix(sharedPrefs) + PREF_OSMOSE_ENABLE_DOWNLOAD, false)) return
        // http://osmose.openstreetmap.fr/en/issues/open.csv?zoom=17&item=xxxx&level=1&limit=500&bbox=16.40570998191834%2C48.179314880149114%2C16.41987204551697%2C48.18563147705161
        // replace bbox
        // try parsing result lines, split each by ','
        val csvUrl = "https://osmose.openstreetmap.fr/en/issues/open.csv"
        val zoom = 16 // what is the use?
        val level = sharedPrefs.getString(questPrefix(sharedPrefs) + PREF_OSMOSE_LEVEL, "1")
        val request = Request.Builder()
            .url("$csvUrl?zoom=$zoom&item=xxxx&level=$level&limit=500&bbox=${bbox.min.longitude}%2C${bbox.min.latitude}%2C${bbox.max.longitude}%2C${bbox.max.latitude}")
            .header("User-Agent", USER_AGENT)
            .build() // any headers necessary?
        Log.i(TAG, "downloading for bbox: $bbox using request ${request.url()}")
        try {
            val response = client.newCall(request).execute()
            val body = response.body() ?: return
            // drop first, it's just column names
            // drop last, it's an empty line (for some reason the size < 14 check didn't work in a test)
            // trim each line because there was some additional newline in logs (maybe windows line endings?)
            val bodylines = body.string().split("\n").drop(1).dropLast(1)
            Log.i(TAG, "got ${bodylines.size} problems")
            // TODO: if multiple elements currently there is a parsing error -> better store them
            //  and highlight them (quest for first element only), so multi-element issues can be shown
            // todo: keep item type and level and block according quests immediately?
            //  level is split[4]
            val blockedItems = sharedPrefs.getString(questPrefix(sharedPrefs) + PREF_OSMOSE_ITEMS, "")!!.split(',')
            db.replaceMany(NAME,
                arrayOf(UUID, ITEM, TITLE, SUBTITLE, ELEMENT_TYPE, ELEMENT_ID, FALSE_POSITIVE),
                bodylines.mapNotNull {
                    val split = it.trim().split(splitRegex) // from https://stackoverflow.com/questions/53997728/parse-csv-to-kotlin-list
                    if (split.size != 14) {
                        Log.i(TAG, "skip line: not split into 14 items: $split")
                        null
                    } else {
                        val key = parseElementKey(split[13])
                        if (key == null) {
                            Log.i(TAG, "skip line: element parse error for ${split[13]}, line: $it")
                            null
                        } else if (blockedItems.contains(split[2])) {
                            Log.i(TAG, "skip line: item type ${split[2]} blocked, line: $it")
                            null
                        } else {
                            Log.i(TAG, "inserting: ${split[0]}, ${split[2]}, ${split[5]}, ${split[6]}, ${key.type}, ${key.id}")
                            arrayOf(split[0], split[2], split[5], split[6], key.type.toString(), key.id, 0)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "exception: ${e.message}", e)
        }
    }

    fun getAll(): Map<ElementKey, OsmoseIssue> {
        val problems = db.query(NAME, where = "$FALSE_POSITIVE == 0") {
            ElementKey(
                ElementType.valueOf(it.getString(ELEMENT_TYPE)),
                it.getLong(ELEMENT_ID)
            ) to
                OsmoseIssue(
                    it.getString(UUID),
                    it.getString(TITLE),
                    it.getString(SUBTITLE),
                    it.getString(ITEM)
                )
        }.toMap()
        Log.i(TAG, "getAll: found ${problems.size} problems")
        return problems
    }

    fun get(element: ElementKey): OsmoseIssue? {
        return db.queryOne(NAME,
            where = "$ELEMENT_TYPE = '${element.type}' AND $ELEMENT_ID = ${element.id} AND $FALSE_POSITIVE = 0",
            columns = arrayOf(UUID, TITLE, SUBTITLE, ITEM)
        ) { OsmoseIssue(it.getString(UUID), it.getString(TITLE), it.getString(SUBTITLE), it.getString(ITEM)) }
    }

    private fun reportChange(uuid: String, falsePositive: Boolean) {
        val url = "https://osmose.openstreetmap.fr/api/0.3/issue/$uuid/" +
            if (falsePositive) "false"
            else "done"
        val request = Request.Builder().url(url).build()
        try {
            client.newCall(request).execute()
            db.delete(NAME, where = "$UUID = '$uuid'")
        } catch (e: IOException) {
            // just do nothing, so it's later tried again (hopefully...)
            Log.i(TAG, "error while uploading: ${e.message} to $url")
        }
    }

    fun reportChanges() {
        Log.i(TAG, "uploading changes")
        val falsePositive = db.query(NAME,
            where = "$FALSE_POSITIVE != 0"
        ) { Pair(
            it.getString(UUID),
            it.getInt(FALSE_POSITIVE) == 1
        ) }
        falsePositive.forEach { reportChange(it.first, it.second) }
    }

    fun setAsFalsePositive(uuid: String, questKey: QuestKey) {
        if (uuid.isEmpty()) return
        db.update(NAME,
            values = listOf(FALSE_POSITIVE to 1),
            where = "$UUID = '$uuid'"
        )
    }

    fun setDone(uuid: String) {
        if (uuid.isEmpty()) return
        db.update(NAME,
            values = listOf(FALSE_POSITIVE to -1),
            where = "$UUID = '$uuid'"
        )
    }

    // for undo: undo all issues for this element. not perfect, but usually enough
    fun setNothing(element: ElementKey) {
        db.update(NAME,
            values = listOf(FALSE_POSITIVE to 0),
            where = "$ELEMENT_TYPE = '${element.type.name}' AND $ELEMENT_ID = ${element.id}"
        )
    }

    fun deleteAll(elements: Collection<ElementKey>) {
        if (elements.isEmpty()) return
        elements.forEach { delete(it) }
    }

    fun delete(element: ElementKey) {
        db.delete(NAME,
            where = "$ELEMENT_TYPE = '${element.type.name}' AND $ELEMENT_ID = ${element.id}"
        )
    }

    private fun parseElementKey(elementString: String): ElementKey? {
        try {
            return when {
                elementString.startsWith("node") -> ElementKey(ElementType.NODE, elementString.substringAfter("node").toLongOrNull() ?: return null)
                elementString.startsWith("way") -> ElementKey(ElementType.WAY, elementString.substringAfter("way").toLongOrNull() ?: return null)
                elementString.startsWith("relation") -> ElementKey(ElementType.RELATION, elementString.substringAfter("relation").toLongOrNull() ?: return null)
                else -> null
            }
        } catch (e: Exception) {
            return null
        }
    }

    fun clear() {
        db.delete(NAME)
    }

}

private const val TAG = "osmose"

data class OsmoseIssue(
    val uuid: String,
    val title: String,
    val subtitle: String,
    val item: String // never used as number
)

object OsmoseTable {
    const val NAME = "osmose_issues"
    private const val NAME_INDEX = "osmose_issues_element_index"

    object Columns {
        const val UUID = "uuid"
        const val TITLE = "title"
        const val SUBTITLE = "subtitle"
        const val ELEMENT_TYPE = "element_type"
        const val ELEMENT_ID = "element_id"
        const val FALSE_POSITIVE = "false_positive"
        const val ITEM = "item"
    }

    const val CREATE_IF_NOT_EXISTS = """
        CREATE TABLE IF NOT EXISTS $NAME (
            $UUID varchar(255) PRIMARY KEY NOT NULL,
            $ELEMENT_ID int NOT NULL,
            $ELEMENT_TYPE varchar(255) NOT NULL,
            $TITLE text,
            $SUBTITLE text,
            $FALSE_POSITIVE int NOT NULL,
            $ITEM int NOT NULL
        );
    """

    const val CREATE_ELEMENT_INDEX_IF_NOT_EXISTS = """
        CREATE INDEX IF NOT EXISTS $NAME_INDEX ON $NAME (
            $ELEMENT_ID,
            $ELEMENT_TYPE
        );
    """

}

private val splitRegex = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()
