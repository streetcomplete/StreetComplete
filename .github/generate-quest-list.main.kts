/**
 * This script generates a CSV file with information about quest types (see `writeCsvFile` function).
 *
 * First, it fetches and parses the table in the OSM Wiki (`WikiQuest`).
 * Then it reads and parses the quest types from the repository code (`RepoQuest`) and
 * matches them to the corresponding WikiQuest (if possible).
 *
 * The generated CSV file contains 3 sections of rows:
 * 1. WikiQuests that could not be matched with RepoQuests
 * 2. RepoQuests that could not be matched with WikiQuests
 * 3. RepoQuests that could be matched with WikiQuests
 *    (note that the "Default Priority" column may be different from the wiki)
 *
 * Note: Run from the project's root directory like this:
 * kotlinc -script -Xplugin=/path/to/kotlinx-serialization-compiler-plugin.jar .github/generate-quest-list.main.kts
 */

@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
@file:Suppress("PLUGIN_IS_NOT_ENABLED")

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URL

val projectDirectory = File(".")
val sourceDirectory = projectDirectory.resolve("app/src/main/java/de/westnordost/streetcomplete/")
val iconsDirectory = projectDirectory.resolve("res/graphics/quest/")

val csvFile = projectDirectory.resolve("quest-list.csv")

val noteQuestName = "OsmNoteQuest"
val noteQuestFile = sourceDirectory.resolve("data/osmnotes/notequests/OsmNoteQuestType.kt")
val noteQuestPackageName = "note_discussion"

val wikiRowSpan2 = " rowspan=\"2\" |"

main()

fun main() {
    val questFileContent = sourceDirectory.resolve("quests/QuestModule.kt").readText()
    val questNameRegex = Regex("(?<=^ {8})[A-Z][a-zA-Z]+(?=\\()", RegexOption.MULTILINE)
    val questNames = listOf(noteQuestName) + questNameRegex.findAll(questFileContent).map { it.value }

    val questFiles = getFilesRecursively(sourceDirectory.resolve("quests/"))
    val strings = getStrings(projectDirectory.resolve("app/src/main/res/values/strings.xml"))
    val wikiQuests = parseWikiTable(getWikiTableContent())
    val repoQuests = questNames.mapIndexed { defaultPriority, name ->
        getQuest(name, defaultPriority, questFiles, strings, wikiQuests)
    }.sortedBy { it.wikiOrder }

    writeCsvFile(repoQuests, wikiQuests)
}

data class RepoQuest(
    val name: String,
    val file: File,
    val icon: File,
    val title: String,
    val defaultPriority: Int,
    val wikiOrder: Int
) {
    val packageName: String get() =
        if (name == noteQuestName) noteQuestPackageName
        else file.parentFile.name

    val csvString: String get() {
        val iconsPath = icon.toRelativeString(projectDirectory).replace(" ", "%20")
        val iconUrl = "https://raw.githubusercontent.com/streetcomplete/StreetComplete/master/$iconsPath"

        val wikiOrder = if (wikiOrder == -1) "\"???\"" else wikiOrder + 1
        return "\"$name\", \"$title\", \"$packageName\", ${defaultPriority + 1}, $wikiOrder, \"$iconUrl\""
    }
}

class WikiQuest(rowCells: List<String>, rowIndex: Int) {
    val wikiOrder: Int = rowIndex
    private val icon: String
    val question: String
    private val askedForElements: String
    private val modifiedTags: String
    private val resurveyInterval: String
    private val defaultPriority: String
    private val sinceVersion: String
    private val notes: String
    private val issueNumber: String?
    private val prNumber: String?
    private val packageName: String?

    init {
        val rowCellContents = rowCells.map {
            var cellContent = it

            if (it.startsWith(wikiRowSpan2)) {
                cellContent = it.substring(wikiRowSpan2.length)
            } else if (it.startsWith(" rowspan=") || it.startsWith(" colspan=")) {
                throw Error("Unsupported rowspan > 2 or colspan detected in table row $rowIndex: $it")
            }

            cellContent.trim()
        }

        val expectedColumnCount = 9
        val receivedColumnCount = rowCellContents.size

        if (receivedColumnCount != expectedColumnCount) {
            val rowString = rowCellContents.joinToString("\n")
            throw Error("Unexpected number of columns ($receivedColumnCount instead of $expectedColumnCount) in table row $rowIndex:\n$rowString")
        }

        icon = rowCellContents[0]
        question = rowCellContents[1]
        askedForElements = rowCellContents[2]
        modifiedTags = rowCellContents[3]
        resurveyInterval = rowCellContents[4]
        defaultPriority = rowCellContents[5]
        sinceVersion = rowCellContents[6]
        notes = rowCellContents[7]

        val code = rowCellContents[8]
        val codeArguments = code.split("|")

        if (!code.startsWith("{{StreetComplete Quest Code|")
            || !code.endsWith("}}")
            || codeArguments.size !in 3..4
        ) {
            throw Error("Unsupported content detected in code column in table row $rowIndex: $code")
        }

        issueNumber = codeArguments[1].ifEmpty { null }
        prNumber = codeArguments[2].ifEmpty { null }
        packageName = codeArguments.getOrNull(3)?.trimEnd { it == '}' }
    }

    fun isOutdated(repoQuests: List<RepoQuest>): Boolean =
        !repoQuests.any { it.wikiOrder == wikiOrder && it.packageName == packageName }

    val csvString: String get() =
        "\"???\", \"$question\", \"${packageName ?: "–"}\", \"???\", ${wikiOrder + 1}, \"???\""
}

fun getFilesRecursively(directory: File): List<File> =
    directory.listFiles()!!.flatMap {
        if (it.isDirectory) getFilesRecursively(it) else listOf(it)
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

fun getQuest(
    questName: String,
    defaultPriority: Int,
    questFiles: List<File>,
    strings: Map<String, String>,
    wikiQuests: List<WikiQuest>
): RepoQuest {
    val file = getQuestFile(questName, questFiles)
    val questFileContent = file.readText()

    val questions = getQuestTitleStringNames(questName, questFileContent).map { strings[it]!! }
    val wikiOrder = wikiQuests.indexOfFirst { questions.contains(it.question) }
    val title = if (wikiOrder > -1) wikiQuests[wikiOrder].question else questions.last()

    val icon = getQuestIcon(questName, questFileContent)

    return RepoQuest(questName, file, icon, title, defaultPriority, wikiOrder)
}

fun getQuestFile(questName: String, questFiles: List<File>): File {
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

fun getQuestIcon(questName: String, questFileContent: String): File {
    val regex = Regex("(?<=override val icon = R\\.drawable\\.ic_quest_)\\w+")
    val iconName = regex.find(questFileContent)?.value
        ?: throw Error("Could not find the icon reference for quest $questName")

    val svgFile = iconsDirectory.resolve("$iconName.svg")

    if (svgFile.exists()) {
        return svgFile
    }

    throw Error("Could not find the SVG for icon '$iconName' (quest $questName).")
}

@Serializable
data class ApiWikiPage(val title: String, val pageid: Long, val wikitext: String)

@Serializable
data class ApiResult(val parse: ApiWikiPage)

fun getWikiTableContent(): String {
    val page = "StreetComplete/Quests"
    val section = 1 // "Released quest types" section

    val apiUrl = URL("https://wiki.openstreetmap.org/w/api.php?action=parse&format=json&prop=wikitext&formatversion=2&page=$page&section=$section")

    val jsonString = apiUrl.openStream().bufferedReader().use { it.readText() }

    return Json.decodeFromString<ApiResult>(jsonString).parse.wikitext
}

fun parseWikiTable(wikiPageContent: String): List<WikiQuest> {
    val tableRows = wikiPageContent.split("\n|-").toMutableList()

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

    return cells.mapIndexed { rowIndex, rowCells -> WikiQuest(rowCells, rowIndex) }
}

fun writeCsvFile(repoQuests: List<RepoQuest>, wikiQuests: List<WikiQuest>) {
    val outdatedWikiQuests = wikiQuests.filter { it.isOutdated(repoQuests) }

    val (updatedRepoQuests, existingRepoQuests) = repoQuests.partition { repoQuest ->
        repoQuest.wikiOrder == -1 // repo quests not yet in wiki
            || outdatedWikiQuests.any { // repo quests not up-to-date in wiki
                it.wikiOrder == repoQuest.wikiOrder
            }
    }

    val csvLines = listOf(
        "\"Quest Name\", \"Question\", \"Package name\", \"Default Priority\", \"Wiki Order\", \"SVG Icon URL\"",
        ",,,,,"
    ) +
        outdatedWikiQuests.map { it.csvString } +
        listOf(",,,,,") +
        updatedRepoQuests.map { it.csvString } +
        listOf(",,,,,") +
        existingRepoQuests.map { it.csvString }

    csvFile.writeText(csvLines.joinToString("\n"))
}
