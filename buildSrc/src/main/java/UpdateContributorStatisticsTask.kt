import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.encodeToStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL

/** Counts the changes made by each author from the git log, merges multiple authors that are linked
 *  to the same github account and writes the result into a JSON file. */
open class UpdateContributorStatisticsTask : DefaultTask() {
    @get:Input lateinit var targetFile: String
    @get:Input var skipCommits: Set<String> = setOf()
    @get:Input var skipCommitRegex: Regex? = null
    @get:Input lateinit var codeFileRegex: Regex
    @get:Input lateinit var assetFileRegex: Regex
    @get:Input lateinit var interfaceMarkupRegex: Regex
    @get:Input lateinit var githubApiToken: String

    @TaskAction fun run() {
        val contributors = getContributors()

        for (contributor in contributors) {
            val githubUser = downloadGithubUserDetails(contributor.hash!!)
            contributor.githubUsername = githubUser?.login
        }

        val mergedContributors = mergeContributorsByGithubAccount(contributors)
            .filter { it.linesOfCodeChanged + it.assetFilesChanged + it.linesOfInterfaceMarkupChanged > 0 }

        writeContributors(mergedContributors.sortedByDescending { it.linesOfCodeChanged })
    }

    private fun getContributors(): Collection<Contributor> {
        val result = mutableMapOf<String, Contributor>()
        val assetFilesChangedByCommit = mutableMapOf<String, Int>()
        val linesOfInterfaceMarkupChangedByCommit = mutableMapOf<String, Int>()
        val linesOfCodeChangedByCommit = mutableMapOf<String, Int>()
        val ignoredCommits = mutableSetOf<String>()

        ProcessBuilder(
            "git",
            "log",
            "--pretty=name: %an%nhash: %H%nsubject: %s%ntimestamp: %at",
            "--numstat",
        ).start().inputStream.bufferedReader().useLines { lines ->
            val ctx = CommitContext()
            for (line in lines) {
                if (!processMetaLine(line, ctx)) {
                    processFileLine(
                        line,
                        ctx,
                        result,
                        assetFilesChangedByCommit,
                        linesOfInterfaceMarkupChangedByCommit,
                        linesOfCodeChangedByCommit,
                        ignoredCommits
                    )
                }
            }
        }

        printStatistics(
            ignoredCommits,
            linesOfCodeChangedByCommit,
            assetFilesChangedByCommit,
            linesOfInterfaceMarkupChangedByCommit
        )

        return result.values
    }

    private fun processMetaLine(line: String, ctx: CommitContext): Boolean = when {
        line.startsWith("name: ") -> {
            ctx.name = line.substringAfter("name: ")
            true
        }
        line.startsWith("hash: ") -> {
            ctx.hash = line.substringAfter("hash: ")
            ctx.timestamp = 0
            true
        }
        line.startsWith("subject: ") -> {
            ctx.subject = line.substringAfter("subject: ")
            true
        }
        line.startsWith("timestamp: ") -> {
            val t = line.substringAfter("timestamp: ").toLongOrNull() ?: 0L
            if (t > ctx.timestamp) ctx.timestamp = t
            true
        }
        else -> false
    }

    @Suppress("LongParameterList")
    private fun processFileLine(
        line: String,
        ctx: CommitContext,
        result: MutableMap<String, Contributor>,
        assetFilesChangedByCommit: MutableMap<String, Int>,
        linesOfInterfaceMarkupChangedByCommit: MutableMap<String, Int>,
        linesOfCodeChangedByCommit: MutableMap<String, Int>,
        ignoredCommits: MutableSet<String>
    ) {
        if (ctx.hash in skipCommits || skipCommitRegex?.matches(ctx.subject) == true) {
            ignoredCommits.add(ctx.hash)
            return
        }

        val splits = line.split(Regex("\\s+"))
        if (splits.size != 3) return

        val contributor = result.getOrPut(ctx.name) { Contributor(ctx.name) }
        val file = splits[2]

        val additions = splits[0].toIntOrNull() ?: 0
        val deletions = splits[1].toIntOrNull() ?: 0
        val changes = additions + deletions

        when {
            codeFileRegex.matches(file) -> {
                linesOfCodeChangedByCommit[ctx.hash] = changes + (linesOfCodeChangedByCommit[ctx.hash] ?: 0)
                contributor.linesOfCodeChanged += changes
            }
            assetFileRegex.matches(file) -> {
                assetFilesChangedByCommit[ctx.hash] = 1 + (assetFilesChangedByCommit[ctx.hash] ?: 0)
                contributor.assetFilesChanged++
            }
            interfaceMarkupRegex.matches(file) -> {
                linesOfInterfaceMarkupChangedByCommit[ctx.hash] = changes + (linesOfInterfaceMarkupChangedByCommit[ctx.hash] ?: 0)
                contributor.linesOfInterfaceMarkupChanged += changes
            }
        }

        if (ctx.timestamp > contributor.lastChangeTimestamp) {
            contributor.lastChangeTimestamp = ctx.timestamp
            contributor.hash = ctx.hash
        }
    }

    private fun printStatistics(
        ignoredCommits: Set<String>,
        linesOfCodeChangedByCommit: Map<String, Int>,
        assetFilesChangedByCommit: Map<String, Int>,
        linesOfInterfaceMarkupChangedByCommit: Map<String, Int>
    ) {
        println("ignored commits")
        println("--------------------------------------------------------")
        ignoredCommits.forEach { println("https://github.com/streetcomplete/StreetComplete/commit/$it") }
        println()
        println("top 50 non-ignored commits with lines of code changed")
        println("--------------------------------------------------------")
        linesOfCodeChangedByCommit.entries.sortedByDescending { it.value }.subList(0, 50)
            .forEach { println("${it.value}\thttps://github.com/streetcomplete/StreetComplete/commit/${it.key}") }
        println()
        println("top 25 non-ignored commits with most asset files changed")
        println("--------------------------------------------------------")
        assetFilesChangedByCommit.entries.sortedByDescending { it.value }.subList(0, 25)
            .forEach { println("${it.value}\thttps://github.com/streetcomplete/StreetComplete/commit/${it.key}") }
        println()
        println("top 50 non-ignored commits with most lines of interface markup changed")
        println("--------------------------------------------------------")
        linesOfInterfaceMarkupChangedByCommit.entries.sortedByDescending { it.value }.subList(0, 50)
            .forEach { println("${it.value}\thttps://github.com/streetcomplete/StreetComplete/commit/${it.key}") }
    }

    private data class CommitContext(
        var name: String = "",
        var hash: String = "",
        var subject: String = "",
        var timestamp: Long = 0L
    )

    private fun downloadGithubUserDetails(hash: String): GithubUser? {
        val url = URL("https://api.github.com/repos/streetcomplete/streetcomplete/commits/$hash")
        val conn = url.openConnection()
        conn.setRequestProperty("Authorization", "token $githubApiToken")
        val response = conn.getInputStream().bufferedReader().readText()
        val jsonFormat = Json { ignoreUnknownKeys = true }
        return jsonFormat.decodeFromString<GithubCommitDetails>(response).author
    }

    private fun mergeContributorsByGithubAccount(contributors: Collection<Contributor>): List<Contributor> {
        val result = mutableListOf<Contributor>()
        val contributorsByGithubUsername = contributors.groupBy { it.githubUsername }
        for ((githubUsername, contributor) in contributorsByGithubUsername) {
            if (githubUsername == null) {
                result.addAll(contributor.map { Contributor(
                    name = it.name,
                    linesOfCodeChanged = it.linesOfCodeChanged,
                    linesOfInterfaceMarkupChanged = it.linesOfInterfaceMarkupChanged,
                    assetFilesChanged = it.assetFilesChanged
                ) })
            } else {
                result.add(Contributor(
                    name = contributor.maxByOrNull { it.lastChangeTimestamp }!!.name,
                    githubUsername = githubUsername,
                    linesOfCodeChanged = contributor.sumOf { it.linesOfCodeChanged },
                    linesOfInterfaceMarkupChanged = contributor.sumOf { it.linesOfInterfaceMarkupChanged },
                    assetFilesChanged = contributor.sumOf { it.assetFilesChanged }
                ))
            }
        }
        return result
    }

    private fun writeContributors(contributors: List<Contributor>) {
        File(targetFile).outputStream().use {
            val yamlFormat = Yaml(configuration = YamlConfiguration(encodeDefaults = false))
            yamlFormat.encodeToStream(contributors, it)
        }
    }
}

@Serializable
data class Contributor(
    val name: String,
    var githubUsername: String? = null,
    var linesOfCodeChanged: Int = 0,
    var linesOfInterfaceMarkupChanged: Int = 0,
    var assetFilesChanged: Int = 0,
    // a commit hash, only used for getting the GitHub account associated with a user
    var hash: String? = null,
    // only used for getting merging contributions made with different email but same GitHub account
    var lastChangeTimestamp: Long = 0,
)

@Serializable
data class GithubCommitDetails(val author: GithubUser?)

@Serializable
data class GithubUser(val login: String, val avatar_url: String)
