
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    @OptIn(ExperimentalSerializationApi::class)
    @TaskAction fun run() {
        val json = Json {
            prettyPrint = true
            prettyPrintIndent = "  "
            encodeDefaults = false
        }
        val targetDir = targetDir?.let { File(it) } ?: return
        val languageCodes = languageCodes ?: return

        for (taxon in Taxon.values()) {
            val taxonDir = File(targetDir, taxon.name.lowercase())

            taxonDir.mkdirs()
            taxonDir.listFiles()?.forEach { it.delete() }

            println("default")
            val scientific = query(null, taxon)
            File(taxonDir, "default.json").writeText(json.encodeToString(scientific))

            for (languageCode in languageCodes) {
                println(languageCode)
                val localized = query(languageCode, taxon)
                if (localized.isEmpty()) continue

                File(taxonDir, "$languageCode.json").writeText(json.encodeToString(localized))
            }
        }
    }

    private fun query(lang: String?, taxon: Taxon): Map<String, TaxonNames> {
        // Query:
        // - its parent taxon (P171) is a direct or indirect subclass (P279) of a tree (Q10884)
        // - its rank in the taxonomic hierarchy (P105) is species (Q7432) or hybrid species (Q1306176)
        //                                                 genus (Q34740) or hybrid genus (Q6045742)
        // - if no languageCode specified: use scientific name (P225) as label, otherwise use localized label

        val ranks = when (taxon) {
            Taxon.Genus -> "wd:Q34740 wd:Q6045742"
            Taxon.Species -> "wd:Q7432 wd:Q1306176"
        }

        val query =
            """
            SELECT DISTINCT ?item ?name ?alt WHERE {
            ?item wdt:P171* ?parent_taxon .
            ?parent_taxon wdt:P279* wd:Q10884 .

            ?item wdt:P105 ?taxon_rank .
            VALUES ?taxon_rank { $ranks } .

            ${
              if (lang == null) "?item wdt:P225 ?name."
              else {
            """
            ?item rdfs:label ?label .
            FILTER (lang(?label) = "$lang") .
            BIND (str(?label) AS ?name) .

            OPTIONAL {
              ?item skos:altLabel ?altLabel .
              FILTER (lang(?altLabel) = "$lang") .
              BIND (str(?altLabel) AS ?alt) .
            }
            """
              }
            }
            }
            """.trimIndent()

        val url = URL("https://query.wikidata.org/sparql?query=${URLEncoder.encode(query, "UTF-8")}")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.setRequestProperty("User-Agent", "StreetComplete")
            connection.setRequestProperty("Accept", "text/tab-separated-values")
            return parseRows(connection.inputStream.bufferedReader().readLines())
        } finally {
            connection.disconnect()
        }
    }

    /** parses e.g. `"http://www.wikidata.org/entity/Q165145 Quercus robur"` to
     *  `"Q165145" to TaxonNameRow("Quercus robur", null)` */
    private fun parseRow(row: String): Pair<String, TaxonNameRow>? {
        val columns = row.split('\t')
        if (columns.size != 3) return null
        val matchResult = wikiDataIdRegex.find(columns[0]) ?: return null
        val id = matchResult.groupValues[0]
        if (id.isEmpty()) return null
        val name = columns[1]
            .trim('"') // value without "..."
            .canonicalizeTaxon()
        if (name.isEmpty()) return null
        val alt = columns[2]
            .trim('"') // value without "..."
            .takeIf { it.isNotEmpty() }
            ?.canonicalizeTaxon()
        return id to TaxonNameRow(name, alt)
    }

    private fun parseRows(rows: List<String>): Map<String, TaxonNames> {
        val r = rows.mapNotNull { parseRow(it) }

        val alts = r
            .groupBy(keySelector = { it.first }, valueTransform = { it.second.alt })
            .mapValues { it.value.filterNotNull() }

        return r.associate { (id, row) ->
            val aliases = alts[id]
                ?.distinct()
                ?.filter { it != row.name }
                .orEmpty()

            id to TaxonNames(row.name, aliases)
        }
    }

    private fun String.canonicalizeTaxon(): String = replace(xRegex, " × ")
}

private enum class Taxon {
    Genus, Species
}

private data class TaxonNameRow(val name: String, val alt: String?)

@Serializable
private data class TaxonNames(val name: String, val aliases: List<String> = emptyList())
