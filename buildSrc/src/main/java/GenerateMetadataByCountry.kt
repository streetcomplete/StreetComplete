import com.esotericsoftware.yamlbeans.YamlConfig
import com.esotericsoftware.yamlbeans.YamlReader
import com.esotericsoftware.yamlbeans.YamlWriter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.support.listFilesOrdered

import java.io.File
import java.io.StringWriter

/** generate [country code].yml files from [property].yml files, for example
 *  popularSports.yml:
 *  DE: [soccer, tennis]
 *
 *  becomes
 *
 *  DE.yml:
 *  popularSports: [soccer, tennis]  */
open class GenerateMetadataByCountry : DefaultTask() {

    @get:Input var sourceDir: String? = null
    @get:Input var targetDir: String? = null

    @TaskAction fun run() {
        val sourceDir = sourceDir?.let { File(it) } ?: return
        val targetDir = targetDir?.let { File(it) } ?: return

        // create / clear target directory
        targetDir.mkdirs()
        targetDir.listFiles()?.forEach { it.delete() }

        val result: MutableMap<String, MutableMap<String, Any>> = mutableMapOf()

        val ymlFiles = sourceDir.listFilesOrdered { it.isFile && it.name.endsWith(".yml") }
        ymlFiles.forEach { file ->
            val property = file.name.substringBeforeLast('.')
            println(property)
            val yaml = YamlReader(file.readText()).read() as Map<String, Any>
            for ((countryCode, value) in yaml) {
                result.getOrPut(countryCode, { mutableMapOf() })[property] = value
            }
        }

        val config = YamlConfig().apply {
            writeConfig.setWriteClassname(YamlConfig.WriteClassName.NEVER)
            writeConfig.isFlowStyle = true
            writeConfig.setEscapeUnicode(false)
        }
        for ((countryCode, byProperty) in result) {
            val targetFile = File(targetDir, "$countryCode.yml")
            val fileWriter = targetFile.writer()
            fileWriter.write("# Do not edit. Source files are in /res/country_metadata\n")
            for ((property, value) in byProperty) {
                val str = StringWriter()
                val writer = YamlWriter(str, config)
                writer.write(value)
                writer.close()
                fileWriter.write("$property: $str")
            }
            fileWriter.close()
        }
    }
}