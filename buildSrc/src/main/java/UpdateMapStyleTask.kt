import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL

/** Pulls the newest map style from the streetcomplete-mapstyle repository */
open class UpdateMapStyleTask : DefaultTask() {

    @get:Input lateinit var targetDir: String
    @get:Input lateinit var mapStyleBranch: String

    @TaskAction fun run() {
        val targetDir = File(targetDir)
        require(targetDir.exists()) { "Directory ${targetDir.absolutePath} does not exist." }

        val githubDirectoryListingUrl = URL("https://api.github.com/repos/streetcomplete/streetcomplete-mapstyle/contents?ref=$mapStyleBranch")
        val mapStyleFiles = fetchDirectoryContents(githubDirectoryListingUrl)

        val excludeFilePaths = setOf(
            ".gitattributes",
            "LICENSE",
            "README.md"
        )

        for (file in mapStyleFiles) {
            if (file.path in excludeFilePaths) {
                continue
            }

            val targetFile = File(targetDir, file.path)
            val downloadUrl = URL(file.download_url)

            if (file.name.endsWith(".yaml")) {
                val fileContent = downloadUrl.readText()
                    .normalizeLineEndings()
                    .replaceOnlineWithLocalSections()

                targetFile.writeText(fileContent)
            } else {
                // e.g. for images: don't try to read/write as UTF-8
                downloadUrl.openStream().use { it.transferTo(targetFile.outputStream()) }
            }
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    private fun fetchDirectoryContents(sourceGithubDirectoryUrl: URL): List<GithubDirectoryListingItem> =
        json.decodeFromString<List<GithubDirectoryListingItem>>(sourceGithubDirectoryUrl.readText())
            .flatMap { if (it.type == "dir") fetchDirectoryContents(URL(it.url)) else listOf(it) }
}

private fun String.normalizeLineEndings() = this.replace("\r\n", "\n")

private fun String.replaceOnlineWithLocalSections(): String {
    val lines = this.split("\n").toMutableList()

    val onlineStartLineIndices = lines.indices.filter { lines[it].trim().startsWith("# for online testing") }
    val localStartLineIndices = lines.indices.filter { lines[it].trim().startsWith("# for local testing") }
    require(onlineStartLineIndices.size == localStartLineIndices.size) {
        "There should be the same number of online sections and local sections"
    }

    val onlineRanges = onlineStartLineIndices.zip(localStartLineIndices)
    onlineRanges.forEach { (start, end) ->
        require(start < end) { "Online section should be before local section" }
    }

    // uncomment local sections
    var shouldUncomment = false
    lines.forEachIndexed { index, line ->
        if (localStartLineIndices.contains(index)) {
            shouldUncomment = true
        } else if (shouldUncomment && line.trim().startsWith("#")) {
            lines[index] = line.replaceFirst("#", "")
        } else {
            shouldUncomment = false
        }
    }

    // delete online sections
    val filteredLines = lines.filterIndexed { index, _ -> !onlineRanges.any { it.contains(index) } }

    return filteredLines.joinToString("\n")
}

private fun Pair<Int, Int>.contains(value: Int) = value in first..second
