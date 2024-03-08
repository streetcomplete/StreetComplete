import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL

const val wikiRowSpan2 = " rowspan=\"2\" |"
const val noteQuestName = "OsmNoteQuest"
const val noteQuestPackageName = "note_discussion"

/**
 * This Gradle task generates a CSV file with information about quest types (see `writeCsvFile` function).
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
 */
open class GenerateQuestListTask : DefaultTask() {
    @get:Input lateinit var targetFile: String
    @get:InputDirectory lateinit var projectDirectory: File
    @get:InputDirectory lateinit var sourceDirectory: File
    @get:InputDirectory lateinit var iconsDirectory: File
    @get:InputFile lateinit var noteQuestFile: File

    private lateinit var wikiQuests: List<WikiQuest>

    @TaskAction
    fun run() {
        wikiQuests = parseWikiTable(getWikiTableContent())

        val questFileContent = sourceDirectory.resolve("quests/QuestsModule.kt").readText()
        val questNameRegex = Regex("(?<=^ {4}\\d+ to )[A-Z][a-zA-Z]+(?=\\()", RegexOption.MULTILINE)
        val questNames =
            listOf(noteQuestName) + questNameRegex.findAll(questFileContent).map { it.value }

        val questFiles = sourceDirectory.resolve("quests/").listFilesRecursively()
        val strings = getStrings(projectDirectory.resolve("app/src/main/res/values/strings.xml"))
        val repoQuests = questNames.mapIndexed { defaultPriority, name ->
            getRepoQuest(name, defaultPriority, questFiles, strings)
        }.sortedBy { it.wikiOrder }

        writeCsvFile(repoQuests)
    }

    private fun getStrings(stringsFile: File): Map<String, String> {
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

    private fun getRepoQuest(
        questName: String,
        defaultPriority: Int,
        questFiles: List<File>,
        strings: Map<String, String>,
    ): RepoQuest {
        val file = getQuestFile(questName, questFiles)
        val questFileContent = file.readText()

        val questions = getQuestTitleStringNames(questName, questFileContent).map { strings[it]!! }
        val wikiOrder = getRepoQuestWikiOrder(questName, questions)
        val title = if (wikiOrder > -1) wikiQuests[wikiOrder].question else questions.last()

        val icon = getQuestIcon(questName, questFileContent)

        return RepoQuest(questName, file, icon, title, defaultPriority, wikiOrder)
    }

    private fun getQuestFile(questName: String, questFiles: List<File>): File {
        if (questName === noteQuestName) {
            return noteQuestFile
        }

        return questFiles.find { it.path.endsWith("$questName.kt") }
            ?: throw Error("Could not find quest file for quest $questName.")
    }

    private fun getQuestTitleStringNames(questName: String, questFileContent: String): List<String> {
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

    private fun getRepoQuestWikiOrder(questName: String, questions: List<String>): Int {
        // first choose the one with an icon description containing the quest name
        val wikiOrder = wikiQuests.indexOfFirst { it.icon.contains(Regex("\\b$questName\\b")) }
        if (wikiOrder > -1) return wikiOrder

        // if not found, choose the one with a matching title
        return wikiQuests.indexOfFirst { questions.contains(it.question) }
    }

    private fun getQuestIcon(questName: String, questFileContent: String): File {
        val regex = Regex("(?<=override val icon = R\\.drawable\\.ic_quest_)\\w+")
        val iconName = regex.find(questFileContent)?.value
            ?: throw Error("Could not find the icon reference for quest $questName")

        val svgFile = iconsDirectory.resolve("$iconName.svg")

        if (svgFile.exists()) {
            return svgFile
        }

        throw Error("Could not find the SVG for icon '$iconName' (quest $questName).")
    }

    private fun getWikiTableContent(): String {
        val page = "StreetComplete/Quests"
        val section = 1 // "Released quest types" section

        val apiUrl = URL("https://wiki.openstreetmap.org/w/api.php?action=parse&format=json&prop=wikitext&formatversion=2&page=$page&section=$section")

        val jsonString = apiUrl.openStream().bufferedReader().use { it.readText() }

        return Json.decodeFromString<ApiResult>(jsonString).parse.wikitext
    }

    private fun parseWikiTable(wikiPageContent: String): List<WikiQuest> {
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

    private fun writeCsvFile(repoQuests: List<RepoQuest>) {
        val outdatedWikiQuests = wikiQuests.filter { it.isOutdated(repoQuests) }

        val (updatedRepoQuests, existingRepoQuests) = repoQuests.partition { repoQuest ->
            // repo quests not yet in wiki
            repoQuest.wikiOrder == -1
            // repo quests not up-to-date in wiki
            || outdatedWikiQuests.any { it.wikiOrder == repoQuest.wikiOrder }
        }

        val csvLines = listOf(
            "\"Quest Name\", \"Question\", \"Package name\", \"Default Priority\", \"Wiki Order\", \"SVG Icon URL\"",
            ",,,,,"
        ) +
            outdatedWikiQuests.map { it.csvString } +
            listOf(",,,,,") +
            updatedRepoQuests.map { it.getCsvString(projectDirectory) } +
            listOf(",,,,,") +
            existingRepoQuests.map { it.getCsvString(projectDirectory) }

        File(targetFile).writeText(csvLines.joinToString("\n"))
    }
}

private data class RepoQuest(
    val name: String,
    val file: File,
    val icon: File,
    val title: String,
    val defaultPriority: Int,
    val wikiOrder: Int,
) {
    val packageName: String get() =
        if (name == noteQuestName) {
            noteQuestPackageName
        } else {
            file.parentFile.name
        }

    fun getCsvString(projectDirectory: File): String {
        val iconsPath = icon.toRelativeString(projectDirectory).replace(" ", "%20")
        val iconUrl = "https://raw.githubusercontent.com/streetcomplete/StreetComplete/master/$iconsPath"

        val wikiOrder = if (wikiOrder == -1) "\"???\"" else wikiOrder + 1
        return "\"$name\", \"$title\", \"$packageName\", ${defaultPriority + 1}, $wikiOrder, \"$iconUrl\""
    }
}

private class WikiQuest(rowCells: List<String>, rowIndex: Int) {
    val wikiOrder: Int = rowIndex
    val icon: String
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
        repoQuests.filter {
            it.wikiOrder == wikiOrder
            && it.packageName == packageName
            && it.title == question
        }.size != 1

    val csvString: String get() =
        "\"???\", \"$question\", \"${packageName ?: "–"}\", \"???\", ${wikiOrder + 1}, \"???\""
}

@Serializable
data class ApiWikiPage(val title: String, val pageid: Long, val wikitext: String)

@Serializable
data class ApiResult(val parse: ApiWikiPage)

private fun File.listFilesRecursively(): List<File> =
    listFiles()!!.flatMap { if (it.isDirectory) it.listFilesRecursively() else listOf(it) }
