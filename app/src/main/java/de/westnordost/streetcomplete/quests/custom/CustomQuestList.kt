package de.westnordost.streetcomplete.quests.custom

import android.content.Context
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuest
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestController
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.util.math.contains
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.Exception

class CustomQuestList(context: Context) : KoinComponent {
    private val entriesById by lazy {
        // need to load by lazy, because there is a problem if mapDataWithEditsSource is accessed early
        val m = hashMapOf<String, CustomQuestEntry>()
        load(m)
        m
    }
    private val path = context.getExternalFilesDir(null)

    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val questController: OtherSourceQuestController by inject()

    init {
        val oldfile = File(path, FILENAME_OLD)
        if (oldfile.exists())
            oldfile.renameTo(File(path, FILENAME_CUSTOM_QUEST))
    }

    fun reload() = load(entriesById)

    fun load(m: MutableMap<String, CustomQuestEntry>) {
        val file = File(path, FILENAME_CUSTOM_QUEST)
        m.clear()
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
        }
        m.putAll(file.readLines().asReversed().mapNotNull { line ->
            val rawText = line.substringAfter(',').substringAfter(',')
            val text = if (rawText.endsWith(",solved"))
                    rawText.substringBeforeLast(',')
                else rawText
            val id = line.getId()
            if (id == null) null
            else
                id to CustomQuestEntry(id).also {
                    it.text = text
                    it.solved = rawText.endsWith(",solved")
                }
        })
    }

    fun addEntry(element: Element, message: String) {
        val id = "${element.type},${element.id}".getId() ?: return
        if (entriesById.containsKey(id)) return
        val entry = CustomQuestEntry(id).apply { text = message }
        entriesById[id] = entry
        val file = File(path, FILENAME_CUSTOM_QUEST)
        file.appendText("\n$id,$message")
        getQuest(id)?.let { questController.addQuests(listOf(it)) }
    }

    fun getEntry(id: String) = entriesById[id]

    fun getQuest(id: String): OtherSourceQuest? {
        val entry = getEntry(id) ?: return null
        if (entry.solved) return null
        val geometry = entry.elementKey?.let { mapDataWithEditsSource.getGeometry(it.type, it.id) }
            ?: entry.position?.let { ElementPointGeometry(it) } ?: return null
        return OtherSourceQuest(
            id,
            geometry,
            questTypeRegistry.getByName(ExternalQuest::class.simpleName!!) as OtherSourceQuestType,
            geometry.center
        ).apply { entry.elementKey?.let { elementKey = it } }
    }

    fun get(bbox: BoundingBox): List<OtherSourceQuest> {
        val type = questTypeRegistry.getByName(ExternalQuest::class.simpleName!!) as OtherSourceQuestType
        return entriesById.values.mapNotNull { entry ->
            if (entry.solved) return@mapNotNull null
            val geometry = entry.elementKey?.let { mapDataWithEditsSource.getGeometry(it.type, it.id) }
                ?: entry.position?.let { ElementPointGeometry(it) } ?: return@mapNotNull null
            if (geometry.center !in bbox) return@mapNotNull null
            OtherSourceQuest(entry.id, geometry, type, geometry.center)
        }
    }

    fun markSolved(id: String, solved: Boolean = true) {
        if (entriesById[id]?.solved == solved) return
        entriesById[id]?.solved = solved
        val file = File(path, FILENAME_CUSTOM_QUEST)
        val lines = file.readLines().toMutableList()
        var lineToChange = -1
        for (i in lines.indices) {
            if (lines[i].getId() == id
                && ((solved && !lines[i].endsWith(",solved"))
                    || !solved && lines[i].endsWith(",solved"))
            ) {
                lineToChange = i
                break
            }
        }
        if (lineToChange == -1) return // should not happen, but crashes also should not happen
        lines[lineToChange] = if (solved) lines[lineToChange] + ",solved"
            else lines[lineToChange].substringBeforeLast(',')
        file.writeText(lines.joinToString("\n"))
    }


    fun deleteSolved() { delete(entriesById.filterValues { it.solved }.map { it.key }) }

    fun delete(id: String) = delete(listOf(id))

    fun delete(idList: List<String>): Boolean {
        if (idList.isEmpty()) return false
        val ids = idList.toMutableSet()
        val deletedAny = entriesById.keys.removeAll(ids)
        val file = File(path, FILENAME_CUSTOM_QUEST)
        val lines = file.readLines().toMutableList()
        val iterator = lines.iterator()
        while (iterator.hasNext()) {
            val id = iterator.next().getId()
            if (id in ids) {
                iterator.remove()
                ids.remove(id)
                if (ids.isEmpty()) break
            }
        }
        file.writeText(lines.joinToString("\n"))
        return deletedAny
    }
}

private fun String.getId(): String? {
    val first = substringBefore(',').trim()
    val second = substringAfter(',').substringBefore(',').trim()
    return if ((first.matches(nodeWayRelation) && second.toLongOrNull() != null) || (first.toDoubleOrNull() != null && second.toDoubleOrNull() != null))
        "${first.uppercase()},${second.uppercase()}"
    else null
}

data class CustomQuestEntry(val id: String ) {
    val elementKey = try {
        ElementKey(ElementType.valueOf(id.substringBefore(',').uppercase()),
            id.substringAfter(',').substringBefore(',').toLong())
    } catch (e: Exception) {
        null
    }
    val position = try {
        LatLon(id.substringBefore(',').toDouble(), id.substringAfter(',').substringBefore(',').toDouble())
    } catch (e: Exception) {
        null
    }
    var text: String = ""
    var solved: Boolean = false
}

const val FILENAME_CUSTOM_QUEST = "custom_quest.csv"
private const val FILENAME_OLD = "external.csv"

private val nodeWayRelation = "node|way|relation".toRegex(RegexOption.IGNORE_CASE)
