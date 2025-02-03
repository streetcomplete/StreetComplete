import Taxon.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

open class UpdateTreeTaxonsTask : DefaultTask() {

    @get:Input var languageCodes: Collection<String>? = null
    @get:Input var targetDir: String? = null

    private val wikiDataIdRegex = Regex("Q[0-9]+")
    private val xRegex = Regex(" *× *")

    @TaskAction fun run() {
        val targetDir = targetDir?.let { File(it) } ?: return
        val languageCodes = languageCodes ?: return

        for (taxon in Taxon.values()) {
            val taxonDir = File(targetDir, taxon.name.lowercase())

            taxonDir.mkdirs()
            taxonDir.listFiles()?.forEach { it.delete() }

            println("default")
            val scientific = query(null, taxon)
            File(taxonDir, "default.yml").writeText(createYaml(scientific))

            for (languageCode in languageCodes) {
                println(languageCode)
                val localized = query(languageCode, taxon)
                    // only include actually localized results
                    .filter { (id, name) ->
                        // ignore case and the ×
                        val scientificName = scientific[id]?.lowercase()?.replace(xRegex," ")
                        val localizedName = name.lowercase().replace(xRegex," ")
                        scientificName != localizedName
                    }
                if (localized.isEmpty()) continue

                File(taxonDir, "$languageCode.yml").writeText(createYaml(localized))
            }
        }
    }

    private fun query(lang: String?, taxon: Taxon): Map<String, String> {
        // TODO why does for example Acer (Maple) not appear in genus? Q42292

        // Query:
        // - its parent taxon (P171) is a direct or indirect subclass (P279) of a tree (Q10884)
        // - its rank in the taxonomic hierarchy (P105) is species (Q7432) or hybrid species (Q1306176)
        //                                                 genus (Q34740) or hybrid genus (Q6045742)
        // - TODO: its name doesn't have the nomenclatural status (P1135) of nomen illegitimum (Q1093954), e.g. Q111626427
        // - TODO: its not a homonymous taxon (P13177) of another taxon that has been named earlier, e.g. Q117157451
        // - if no languageCode specified: use scientific name (P225) as label, otherwise use localized label

        val ranks = when (taxon) {
            Genus -> "wd:Q34740 wd:Q6045742"
            Species -> "wd:Q7432 wd:Q1306176"
        }

        val query = """
            SELECT DISTINCT ?item ?name WHERE {
            ?item wdt:P171 ?parent_taxon .
            ?parent_taxon wdt:P279* wd:Q10884 .

            ?item wdt:P105 ?taxon_rank .
            VALUES ?taxon_rank { $ranks } .

            ${
              if (lang == null) "?item wdt:P225 ?name."
              else "?item rdfs:label ?label . FILTER (lang(?label) = \"$lang\") . BIND (str(?label) AS ?name) ."
            }
            }
            """.trimIndent()

        val url = URL("https://query.wikidata.org/sparql?query=${URLEncoder.encode(query, "UTF-8")}")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.setRequestProperty("User-Agent", "StreetComplete")
            connection.setRequestProperty("Accept", "text/tab-separated-values")
            return connection.inputStream
                .bufferedReader()
                .readLines()
                .mapNotNull { parseRow(it) }
                .sortedBy { it.second }
                .toMap()
        } finally {
            connection.disconnect()
        }
    }

    /** parses e.g. `"http://www.wikidata.org/entity/Q165145 Quercus robur"` to
     *  `"Q165145" to "Quercus robur"` */
    private fun parseRow(row: String): Pair<String, String>? {
        val columns = row.split('\t')
        if (columns.size != 2) return null
        val matchResult = wikiDataIdRegex.find(columns[0]) ?: return null
        val id = matchResult.groupValues[0]
        if (id.isEmpty()) return null
        val name = columns[1].trim('"') // value without "..."
        if (name.isEmpty()) return null
        return id to name
    }

    private fun createYaml(map: Map<String, String>): String {
        return map.entries.joinToString("\n") { it.key + ": " + it.value }
    }
}

private enum class Taxon {
    Genus, Species
}
