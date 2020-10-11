package de.westnordost.streetcomplete

import de.westnordost.countryboundaries.CountryBoundaries
import java.io.FileInputStream
import java.io.FileWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// as param?!
// TODO file out as def
// TODO key value as def? as param?
// TODO cap min as def...

fun main() {
    val boundaries = CountryBoundaries.load(FileInputStream("app/src/main/assets/boundaries.ser"))
    val query = """
        SELECT ?operator ?loc
        WHERE { ?osm osmt:amenity "atm"; osmt:operator ?operator; osmm:loc ?loc. }
        """.trimIndent()

    val result: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()

    val rows = querySophox(query).mapNotNull { parseRow(it) }
    for (row in rows) {
        val countryCode = boundaries.getIds(row.lon, row.lat).firstOrNull()
        if (countryCode != null) {
            result
                .getOrPut(countryCode, { mutableMapOf() })
                .compute(row.operator) { _, u ->  (u ?: 0) + 1}
        }
    }

    val writer = FileWriter("atm_operators.yaml", false)
    for (countryCode in result.keys.sorted()) {
        writer.appendln("$countryCode:")
        val entries = result[countryCode]!!.entries.sortedByDescending { it.value }
        for (entry in entries) {
            writer.appendln("  - ${entry.key}: ${entry.value}")
        }
    }
    writer.close()
}

private val pointRegex = Regex("Point\\(([-+\\d.]*) ([-+\\d.]*)\\)")

private fun querySophox(query: String): List<String> {
    val url = URL("https://sophox.org/sparql?query="+URLEncoder.encode(query,"UTF-8"))
    val connection = url.openConnection() as HttpURLConnection
    try {
        connection.setRequestProperty("Accept", "text/csv")
        connection.setRequestProperty("User-Agent", "StreetComplete")
        connection.setRequestProperty("charset", StandardCharsets.UTF_8.name())
        connection.doOutput = true
        return connection.inputStream.bufferedReader().readLines()
    } finally {
        connection.disconnect()
    }
}

private fun parseRow(row: String): Row? {
    val elements = row.split(',')
    val name = elements[0]
    if (elements.size < 2) return null
    val matchResult = pointRegex.matchEntire(elements[1]) ?: return null
    val lon = matchResult.groupValues[1].toDoubleOrNull() ?: return null
    val lat = matchResult.groupValues[2].toDoubleOrNull() ?: return null
    return Row(name, lon, lat)
}

private data class Row(val operator: String, val lon: Double, val lat: Double)