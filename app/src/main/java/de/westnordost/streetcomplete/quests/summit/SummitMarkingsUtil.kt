package de.westnordost.streetcomplete.quests.summit

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept

val COUNTRIES_WHERE_SUMMIT_MARKINGS_ARE_COMMON = NoCountriesExcept(
    // regions gathered in
    // https://github.com/streetcomplete/StreetComplete/issues/561#issuecomment-325623974

    // Europe
    "AT", // https://de.wikipedia.org/wiki/Gipfelkreuz
    "CH", // https://de.wikipedia.org/wiki/Gipfelkreuz
    "CZ", // https://cs.wikipedia.org/wiki/Vrcholov%C3%A1_kniha
    "DE", // https://de.wikipedia.org/wiki/Gipfelbuch
    "ES", // https://es.wikipedia.org/wiki/Comprobante_de_cumbre
    "FR", // https://it.wikipedia.org/wiki/Libro_di_vetta#Alcuni_esempi_di_libri_di_vetta
    "GR", // https://it.wikipedia.org/wiki/Libro_di_vetta#Alcuni_esempi_di_libri_di_vetta
    "IT", // https://it.wikipedia.org/wiki/Libro_di_vetta
    // not "NL": https://nl.wikipedia.org/wiki/Gipfelbuch is about "foreign" summit registers e.g. in the Alps
    // not "PL": https://github.com/westnordost/StreetComplete/issues/561#issuecomment-325504455
    "RO", // https://es.wikipedia.org/wiki/Cruz_de_la_cumbre#Ejemplos
    "SI", // https://it.wikipedia.org/wiki/Libro_di_vetta#Alcuni_esempi_di_libri_di_vetta
    "SK", // https://it.wikipedia.org/wiki/Croce_di_vetta#Alcuni_esempi_di_croci_di_vetta

    // Americas
    "AR", // https://en.wikipedia.org/wiki/Summit_cross#Gallery
    "PE", // https://es.wikipedia.org/wiki/Cruz_de_la_cumbre#Ejemplos
    "US", // https://de.wikipedia.org/wiki/Gipfelkreuz
)

private val hikingPathsFilter by lazy { """
    ways with
      highway = path
      and sac_scale ~ mountain_hiking|demanding_mountain_hiking|alpine_hiking|demanding_alpine_hiking|difficult_alpine_hiking
""".toElementFilterExpression() }

private fun getHikingPaths(mapData: MapDataWithGeometry) =
    mapData.ways.filter { hikingPathsFilter.matches(it) }
        .mapNotNull { mapData.getWayGeometry(it.id) as? ElementPolylinesGeometry }

private fun getHikingRoutes(mapData: MapDataWithGeometry) =
    mapData.relations.filter { it.tags["route"] == "hiking" }
        .mapNotNull { mapData.getRelationGeometry(it.id) as? ElementPolylinesGeometry }

fun getHikingPathsAndRoutes(mapData: MapDataWithGeometry) =
    getHikingPaths(mapData) + getHikingRoutes(mapData)
