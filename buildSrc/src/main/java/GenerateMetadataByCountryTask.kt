import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
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
        val yamlFormat = Yaml {
            listSerialization = YamlBuilder.ListSerialization.FLOW_SEQUENCE
            stringSerialization = YamlBuilder.StringSerialization.SINGLE_QUOTATION
        }
        for ((countryCode, valuesByProperty) in metadataByCountry) {
            val yamlString = yamlFormat.encodeToString(valuesByProperty.toSortedMap())
                .trim()
                .replace("\n\n", "\n")

            val targetFile = File(targetDir, "$countryCode.yml")
            val fileWriter = targetFile.writer()
            fileWriter.write("# Do not edit. Data is from res/country_metadata and https://github.com/streetcomplete/countrymetadata\n")
            fileWriter.write("$yamlString\n")
            fileWriter.close()
        }
    }

    /** Read country metadata. Returns map of file name -> contents */
    private fun readLocalCountryMetadata(sourceDir: File): Map<String, Map<String, Any>> =
        sourceDir.listFiles().orEmpty()
            .filter { it.isFile && it.name.endsWith(".yml") }
            .associate { it.name.withoutExtension to Yaml.decodeFromString(it.readText()) }

    private val json = Json { ignoreUnknownKeys = true }

    /** Fetch country metadata. Returns map of file name -> contents */
    private fun fetchCountryMetadata(sourceGithubDirectoryUrl: URL): Map<String, Map<String, Any>> =
        json.decodeFromString<List<GithubDirectoryListingItem>>(sourceGithubDirectoryUrl.readText())
            .filter { it.type == "file" && it.name.endsWith(".yml") }
            .associate {
                val response = URL(it.download_url).readText()
                it.name.withoutExtension to Yaml.decodeFromString("$response\n")
            }
}

private val String.withoutExtension get() = substringBeforeLast('.')
