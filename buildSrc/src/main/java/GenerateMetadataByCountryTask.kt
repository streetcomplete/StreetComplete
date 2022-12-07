import com.esotericsoftware.yamlbeans.YamlConfig
import com.esotericsoftware.yamlbeans.YamlReader
import com.esotericsoftware.yamlbeans.YamlWriter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.StringWriter
import java.net.URL

/** generate [country code].yml files from [property].yml files, for example
 *  popularSports.yml:
 *  DE: [soccer, tennis]
 *
 *  becomes
 *
 *  DE.yml:
 *  popularSports: [soccer, tennis]  */
open class GenerateMetadataByCountryTask : DefaultTask() {

    @get:Input var sourceDir: String? = null
    @get:Input var targetDir: String? = null

    @TaskAction fun run() {
        val sourceDir = sourceDir?.let { File(it) } ?: return
        val targetDir = targetDir?.let { File(it) } ?: return
        val githubDirectoryListingUrl = URL("https://api.github.com/repos/streetcomplete/countrymetadata/contents/data")

        // create / clear target directory
        targetDir.mkdirs()
        targetDir.listFiles()?.forEach { it.delete() }

        // source data: map of property -> country code -> value
        val metadataByProperty =
            fetchCountryMetadata(githubDirectoryListingUrl) + readLocalCountryMetadata(sourceDir)

        // target data: map of country code -> property -> value
        val metadataByCountry: MutableMap<String, MutableMap<String, Any>> = mutableMapOf()
        for ((property, valuesByCountry) in metadataByProperty) {
            for ((countryCode, value) in valuesByCountry) {
                metadataByCountry.getOrPut(countryCode, { mutableMapOf() })[property] = value
            }
        }

        writeCountryMetadata(metadataByCountry)
    }

    private fun writeCountryMetadata(metadataByCountry: Map<String, Map<String, Any>>) {
        val config = YamlConfig().apply {
            writeConfig.setWriteClassname(YamlConfig.WriteClassName.NEVER)
            writeConfig.isFlowStyle = true
            writeConfig.setWrapColumn(Int.MAX_VALUE)
            writeConfig.setEscapeUnicode(false)
        }
        for ((countryCode, valuesByProperty) in metadataByCountry) {
            val targetFile = File(targetDir, "$countryCode.yml")
            val fileWriter = targetFile.writer()
            fileWriter.write("# Do not edit. Data is from res/country/metadata and https://github.com/streetcomplete/countrymetadata\n")
            for ((property, value) in valuesByProperty.toSortedMap()) {
                val str = StringWriter()
                val writer = YamlWriter(str, config)
                writer.write(value)
                writer.close()
                fileWriter.write("$property: $str")
            }
            fileWriter.close()
        }
    }

    /** Read country metadata. Returns map of file name -> contents */
    private fun readLocalCountryMetadata(sourceDir: File): Map<String, Map<String, Any>> =
        sourceDir.listFiles().orEmpty()
            .filter { it.isFile && it.name.endsWith(".yml") }
            .associate { it.name.withoutExtension to YamlReader(it.readText()).read() as Map<String, Any> }

    private val json = Json { ignoreUnknownKeys = true }

    /** Fetch country metadata. Returns map of file name -> contents */
    private fun fetchCountryMetadata(sourceGithubDirectoryUrl: URL): Map<String, Map<String, Any>> =
        json.decodeFromString<List<GithubDirectoryListingItem>>(sourceGithubDirectoryUrl.readText())
            .filter { it.type == "file" && it.name.endsWith(".yml") }
            .associate {
                val response = URL(it.download_url).readText()
                it.name.withoutExtension to YamlReader(response).read() as Map<String, Any>
            }
}

private val String.withoutExtension get() = substringBeforeLast('.')
