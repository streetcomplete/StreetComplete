/**
 * Note: Run from the project's root directory like this:
 * kotlinc -script -Xplugin=/path/to/kotlinx-serialization-compiler-plugin.jar .github/generate-quest-list.main.kts
 */

@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
@file:Suppress("PLUGIN_IS_NOT_ENABLED")

import java.io.File
import java.net.URL
import kotlinx.serialization.*
import kotlinx.serialization.json.*

val projectDirectory = File(".")
val sourceDirectory = projectDirectory.resolve("app/src/main/java/de/westnordost/streetcomplete/")

val csvFile = projectDirectory.resolve("quest-list.csv")

val noteQuestName = "OsmNoteQuest"
val noteQuestFile = sourceDirectory.resolve("data/osmnotes/notequests/OsmNoteQuestType.kt")

val wikiRowSpan2 = " rowspan=\"2\" |"

val questFileContent = sourceDirectory.resolve("quests/QuestModule.kt").readText()
val questNameRegex = Regex("(?<=^ {8})[A-Z][a-zA-Z]+(?=\\()", RegexOption.MULTILINE)
val questNames = listOf(noteQuestName) + questNameRegex.findAll(questFileContent).map { it.value }

val questFiles: List<File> = getFilesRecursively(sourceDirectory.resolve("quests/"))
val strings: Map<String, String> = getStrings(projectDirectory.resolve("app/src/main/res/values/strings.xml"))
val wikiTable: List<WikiTableQuest> = parseWikiTable(getWikiTableContent())
val quests: List<RepoQuest> = questNames.mapIndexed { defaultPriority, name -> getQuest(name, defaultPriority) }.sortedBy { it.wikiOrder }

writeCsvFile()


data class RepoQuest(
    val name: String,
    val file: File,
    val title: String,
    val defaultPriority: Int,
    val wikiOrder: Int
) {
    val csvString: String get() {
        val wikiOrder = if (wikiOrder == -1) "\"???\"" else wikiOrder
        return "\"$name\", \"$title\", ${defaultPriority + 1}, $wikiOrder"
    }
}

class WikiTableQuest(rowCells: List<String>, rowIndex: Int) {
    private val wikiOrder: Int = rowIndex
    private val icon: String
    val question: String
    private val askedForElements: String
    private val modifiedTags: String
    private val defaultPriority: String
    private val sinceVersion: String
    private val notes: String
    private val code: String

    init {
        val rowCellContents = rowCells.map {
            var cellContent = it

            if (it.startsWith(wikiRowSpan2)) {
                cellContent = it.substring(wikiRowSpan2.length)
            }
            else if (it.startsWith(" rowspan=") || it.startsWith(" colspan=")) {
                throw Error("Unsupported rowspan > 2 or colspan detected in table row $rowIndex: $it")
            }

            cellContent.trim()
        }

        icon = rowCellContents[0]
        question = rowCellContents[1]
        askedForElements = rowCellContents[2]
        modifiedTags = rowCellContents[3]
        defaultPriority = rowCellContents[4]
        sinceVersion = rowCellContents[5]
        notes = rowCellContents[6]
        code = rowCellContents[7]
    }

    val isOutdated: Boolean get() = !quests.any { it.wikiOrder == wikiOrder }

    val csvString: String get() = "\"???\", \"$question\", \"???\", $wikiOrder"
}

fun getFilesRecursively(directory: File): List<File> {
    return directory.listFiles()!!.flatMap {
        if (it.isDirectory) getFilesRecursively(it) else listOf(it)
    }
}

fun getStrings(stringsFile: File): Map<String, String> {
    fun normalizeString(string: String) = string
        .trim('"') // strip optional quotes around the string
        .replace("\\n", "\n") // replace \n with real newline characters
        .replace(Regex("\\\\(['\"])"), "$1") // unescape quotes
        .replace(Regex("%s|%\\d\\\$s"), "[…]") // replace variables
        .replace("([…])", "[…]")
        .replace("[…] […]", "[…]")

    val stringRegex = Regex("<string name=\"([^\"]+)\">(.*?)</string>", RegexOption.DOT_MATCHES_ALL)
    val stringsContent = stringsFile.readText()

    return stringRegex.findAll(stringsContent).map { it.groupValues[1] to normalizeString(it.groupValues[2]) }.toMap()
}

fun getQuest(questName: String, defaultPriority: Int): RepoQuest {
    val file = getQuestFile(questName)
    val questFileContent = file.readText()

    val questions = getQuestTitleStringNames(questName, questFileContent).map { strings[it]!! }

    val wikiOrder = wikiTable.indexOfFirst { questions.contains(it.question) }
    val title = if (wikiOrder > -1) wikiTable[wikiOrder].question else questions.last()

    return RepoQuest(questName, file, title, defaultPriority, wikiOrder)
}

fun getQuestFile(questName: String): File {
    if (questName === noteQuestName) {
        return noteQuestFile
    }

    return questFiles.find { it.path.endsWith("$questName.kt") }
        ?: throw Error("Could not find quest file for quest $questName.")
}

fun getQuestTitleStringNames(questName: String, questFileContent: String): List<String> {
    val regex = Regex("(?<=R\\.string\\.)quest_\\w+")
    val stringResourceNames = regex.findAll(questFileContent).toList().map { it.value }

    if (stringResourceNames.isEmpty()) {
        throw Error("Could not find the title string reference for quest $questName.")
    }

    if (stringResourceNames.size == 1) {
        return stringResourceNames
    }

    return stringResourceNames.filter { it.contains("title") }
}

@Serializable
data class ApiResult(val parse: ApiWikiPage)

@Serializable
data class ApiWikiPage(val title: String, val pageid: Long, val wikitext: String)

fun getWikiTableContent(): String {
    val page = "StreetComplete/Quests"
    val section = 1 // "Released quest types" section

    val apiUrl = URL("https://wiki.openstreetmap.org/w/api.php?action=parse&format=json&prop=wikitext&formatversion=2&page=$page&section=$section")

    val jsonString = apiUrl.openStream().bufferedReader().use { it.readText() }

    return Json.decodeFromString<ApiResult>(jsonString).parse.wikitext
}

fun parseWikiTable(wikiPageContent: String): List<WikiTableQuest> {
    val tableRows = wikiPageContent.split("|-").toMutableList()

    tableRows.removeFirst() // Drop table header and everything before the table
    tableRows[tableRows.size - 1] = tableRows.last().split("|}")[0] // Drop everything after the table

    val cells = mutableListOf<List<String>>()

    // Fill "rowspan"ed columns
    tableRows.forEachIndexed { rowIndex, row ->
        val rowCells = row.split("\n|").toMutableList()
        rowCells.removeFirst() // Drop empty string between table row start `|-` and first column start `|`

        if (rowIndex > 0) {
            cells[rowIndex - 1].forEachIndexed { cellIndex, cell ->
                if (cell.startsWith(wikiRowSpan2)) {
                    rowCells.add(cellIndex, cell.substring(wikiRowSpan2.length))
                }
            }
        }

        cells += rowCells
    }

    return cells.mapIndexed { rowIndex, rowCells -> WikiTableQuest(rowCells, rowIndex) }
}

fun writeCsvFile() {
    val newQuests = quests.filter { it.wikiOrder == -1 }
    val oldQuests = quests.filter { it.wikiOrder > -1 }

    val csvLines =
        listOf("\"Quest Name\", \"Question\", \"Default Priority\", \"Wiki Order\"") +
        wikiTable.filter { it.isOutdated }.map { it.csvString } +
        listOf(",,,") +
        newQuests.map { it.csvString } +
        listOf(",,,") +
        oldQuests.map { it.csvString }

    csvFile.writeText(csvLines.joinToString("\n"))
}
