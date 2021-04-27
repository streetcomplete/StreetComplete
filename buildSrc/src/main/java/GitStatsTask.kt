import com.esotericsoftware.yamlbeans.YamlConfig
import com.esotericsoftware.yamlbeans.YamlWriter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.FileInputStream
import java.io.FileWriter
import java.io.StringWriter
import java.nio.charset.StandardCharsets

/** Counts the occurance of changes in git commits for a given regex and
 *  writes the result in a YML file. */
open class GitStatsTask : DefaultTask() {
    @get:Input lateinit var targetFile: String
    @get:Input lateinit var commitFileFilter: Regex
  
    @TaskAction fun run() {
        val countsByName = mutableMapOf<String, Int>()
        val countsByCommit = mutableMapOf<String, Int>()
        Runtime.getRuntime().exec("git log --no-merges --pretty='%an'%n%H --numstat").inputStream.bufferedReader().useLines { lines ->
            var name = ""
            var commit = ""
            var commitNext = false
            var skipNext = false
            for(line in lines) {
                if (line.startsWith('\'')) {
                    name = line.trim('\'')
                    skipNext = false
                    commitNext = true
                } else if (commitNext) {
                    commit = line
                    if (line.startsWith("ae7a244dd60ccfc91cf2dc01bf9e60c8d6a81616")) {
                      // println("Found commit " + line)
                      skipNext = true
                    }
                    commitNext = false
                } else {
                    val splits = line.split(Regex("\\s+"))
                    val additions = splits[0].toIntOrNull() ?: continue
                    val deletions = splits[1].toIntOrNull() ?: continue
                    if (!splits.last().matches(commitFileFilter)) continue
                    val commitTotal = additions + deletions
                    if (!skipNext) {
                        countsByName[name] = commitTotal + countsByName.getOrPut(name, { 0 })
                    }
                    if (commitTotal > 400) {
                        countsByCommit[commit] = commitTotal
                    }
                }
            }
        }
        countsByCommit.entries.sortedByDescending { it.value }.forEach { println("${it.value}\t${it.key}") }
        println("*************************************************************")
        countsByName.entries.sortedByDescending { it.value }.forEach { println("${it.value}\t${it.key}") }
    }
}
