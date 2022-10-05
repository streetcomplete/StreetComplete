package de.westnordost.streetcomplete.quests.external

import android.content.Context
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import java.io.File
import java.lang.Exception

class ExternalList(context: Context) {
    val questsMap = mutableMapOf<ElementKey, String>()
    private val path = context.getExternalFilesDir(null)

    init { reload() }

    fun reload() {
        val file = File(path, FILENAME_EXTERNAL)
        questsMap.clear()
        if (!file.exists()) return
        questsMap.putAll(file.readLines().mapNotNull {
            val elementType = it.substringBefore(',').trim()
            val rest = it.substringAfter(',').trim()
            val elementId = rest.substringBefore(',').trim()
            val text =
                if (rest.contains(','))
                    rest.substringAfter(',').trim()
                else ""

            try {
                ElementKey(ElementType.valueOf(elementType), elementId.toLong()) to text
            } catch(e: Exception) {
                null
            }
        }
        )
    }

    fun remove(key: ElementKey) {
        questsMap.remove(key)
        val file = File(path, FILENAME_EXTERNAL)
        val lines = file.readLines().toMutableList()
        lines.removeAll { line ->
            if (!line.contains(','))
                false
            else {
                val lineSplit = line.split(",").map { it.trim() }
                lineSplit[1].toLong() == key.id && lineSplit[0] == key.type.name
            }
        }
        file.writeText(lines.joinToString("\n"))
    }
}

const val FILENAME_EXTERNAL = "external.csv"
