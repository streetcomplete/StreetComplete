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
            var name = ""
            var hash = ""
            var subject = ""
            var timestamp = 0L
            for (line in lines) {
                when {
                    line.startsWith("name: ") -> {
                        name = line.substringAfter("name: ")
                    }
                    line.startsWith("hash: ") -> {
                        hash = line.substringAfter("hash: ")
                        timestamp = 0
                    }
                    line.startsWith("subject: ") -> {
                        subject = line.substringAfter("subject: ")
                    }
                    line.startsWith("timestamp: ") -> {
                        val t = line.substringAfter("timestamp: ").toLongOrNull() ?: 0L
                        if (t > timestamp) timestamp = t
                    }
                    else -> {
                        if (hash in skipCommits || skipCommitRegex?.matches(subject) == true) {
                            ignoredCommits.add(hash)
                            continue
                        }

                        val splits = line.split(Regex("\\s+"))
                        if (splits.size != 3) continue

                        val contributor = result.getOrPut(name) { Contributor(name) }
                        val file = splits[2]

                        val additions = splits[0].toIntOrNull() ?: 0
                        val deletions = splits[1].toIntOrNull() ?: 0
                        val changes = additions + deletions

                        when {
                            codeFileRegex.matches(file) -> {
                                linesOfCodeChangedByCommit[hash] = changes + (linesOfCodeChangedByCommit[hash] ?: 0)
                                contributor.linesOfCodeChanged += changes
                            }
                            assetFileRegex.matches(file) -> {
                                assetFilesChangedByCommit[hash] = 1 + (assetFilesChangedByCommit[hash] ?: 0)
                                contributor.assetFilesChanged++
                            }
                            interfaceMarkupRegex.matches(file) -> {
                                linesOfInterfaceMarkupChangedByCommit[hash] = changes + (linesOfInterfaceMarkupChangedByCommit[hash] ?: 0)
                                contributor.linesOfInterfaceMarkupChanged += changes
                            }
                        }
                        if (timestamp > contributor.lastChangeTimestamp) {
                            contributor.lastChangeTimestamp = timestamp
                            contributor.hash = hash
                        }
                    }
                }
            }
        }
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

        return result.values
    }

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
