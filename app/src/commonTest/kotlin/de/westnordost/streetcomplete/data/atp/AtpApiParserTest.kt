package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.io.Buffer
import kotlinx.io.writeString
import kotlin.test.Test
import kotlin.test.assertEquals

class AtpApiParserTest {
    @Test
    fun `parse empty response`() {
        val buffer = Buffer()
        // https://bbox-filter-for-atp.bulwersator-cloudflare.workers.dev/api/entries?lat_min=50.3&lat_max=50.33&lon_min=19.9&lon_max=19.95
        buffer.writeString("""[]""")
        assertEquals(listOf(), AtpApiParser().parseAtpEntries(buffer))
    }

    @Test
    fun `parse one minimum atp entry`() {
        val buffer = Buffer()
        // https://bbox-filter-for-atp.bulwersator-cloudflare.workers.dev/api/entries?lat_min=50.2839&lat_max=50.35&lon_min=19.9&lon_max=19.95
        buffer.writeString("""[
  {
    "atp_entry_id": -3097502835224812,
    "atp_center_lat": 50.337333,
    "atp_center_lon": 19.926502,
    "atp_tags": "{\"ref\": \"AL004KMI\", \"description\": \"Na placu\", \"@source_uri\": \"https://edge.allegro.pl/general-deliveries/2595554\", \"@spider\": \"allegro_one_box_pl\", \"amenity\": \"parcel_locker\", \"addr:street_address\": \"Go\\u0142cza 52\", \"addr:city\": \"Go\\u0142cza\", \"addr:country\": \"PL\", \"website\": \"https://allegro.pl/kampania/one/znajdz-nas?pointId=2595554\", \"opening_hours\": \"24/7\", \"brand\": \"Allegro One Box\", \"brand:wikidata\": \"Q110738715\", \"atp_id\": \"joZ8fm6HJhR0l2oH6b9TgijR_EI=\", \"atp_ref\": \"AL004KMI\"}",
    "osm_match_center_lat": null,
    "osm_match_center_lon": null,
    "osm_match_tags": "null",
    "osm_element_match_id": null,
    "osm_element_match_type": null,
    "match_distance": null,
    "all_very_good_matches": "null",
    "report_type": "MISSING_POI_IN_OPENSTREETMAP",
    "is_marked_as_bad_by_mapper": 0
  }
]"""
        )

        val atpEntry = AtpEntry(
            position = LatLon(50.337333, 19.926502),
            id = -3097502835224812,
            osmMatch = null,
            tagsInATP = mapOf(
                "ref" to "AL004KMI",
                "description" to "Na placu",
                "@source_uri" to "https://edge.allegro.pl/general-deliveries/2595554",
                "@spider" to "allegro_one_box_pl",
                "amenity" to "parcel_locker",
                "addr:street_address" to "Gołcza 52",
                "addr:city" to "Gołcza",
                "addr:country" to "PL", "website" to "https://allegro.pl/kampania/one/znajdz-nas?pointId=2595554",
                "opening_hours" to "24/7", "brand" to "Allegro One Box",
                "brand:wikidata" to "Q110738715",
                "atp_id" to "joZ8fm6HJhR0l2oH6b9TgijR_EI=",
                "atp_ref" to "AL004KMI"
            ),
            tagsInOSM = null,
            reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP
        )

        assertEquals(listOf(atpEntry), AtpApiParser().parseAtpEntries(buffer))
    }

    @Test
    fun `parse two more complete atp entries`() {
        val buffer = Buffer()
        // https://bbox-filter-for-atp.bulwersator-cloudflare.workers.dev/api/entries?lat_min=50.2838&lat_max=50.35&lon_min=19.9&lon_max=19.95
        buffer.writeString("""[
  {
    "atp_entry_id": -3097502835224812,
    "atp_center_lat": 50.337333,
    "atp_center_lon": 19.926502,
    "atp_tags": "{\"ref\": \"AL004KMI\", \"description\": \"Na placu\", \"@source_uri\": \"https://edge.allegro.pl/general-deliveries/2595554\", \"@spider\": \"allegro_one_box_pl\", \"amenity\": \"parcel_locker\", \"addr:country\": \"PL\", \"website\": \"https://allegro.pl/kampania/one/znajdz-nas?pointId=2595554\", \"opening_hours\": \"24/7\", \"brand\": \"Allegro One Box\", \"brand:wikidata\": \"Q110738715\", \"atp_id\": \"joZ8fm6HJhR0l2oH6b9TgijR_EI=\", \"atp_ref\": \"AL004KMI\"}",
    "osm_match_center_lat": null,
    "osm_match_center_lon": null,
    "osm_match_tags": "null",
    "osm_element_match_id": null,
    "osm_element_match_type": null,
    "match_distance": null,
    "all_very_good_matches": "null",
    "report_type": "MISSING_POI_IN_OPENSTREETMAP",
    "is_marked_as_bad_by_mapper": 0
  },
  {
    "atp_entry_id": 1834581365633738,
    "atp_center_lat": 50.283805,
    "atp_center_lon": 19.920721,
    "atp_tags": "{\"ref\": \"AL005KMI\", \"description\": \"Na placu\", \"@source_uri\": \"https://edge.allegro.pl/general-deliveries/2593315\", \"@spider\": \"allegro_one_box_pl\", \"amenity\": \"parcel_locker\", \"addr:country\": \"PL\", \"website\": \"https://allegro.pl/kampania/one/znajdz-nas?pointId=2593315\", \"opening_hours\": \"24/7\", \"brand\": \"Allegro One Box\", \"brand:wikidata\": \"Q110738715\", \"atp_id\": \"G5H5NoXhe_2QjVhvZt9LjxzJ6sE=\", \"atp_ref\": \"AL005KMI\"}",
    "osm_match_center_lat": 50.28380,
    "osm_match_center_lon": 19.92072,
    "osm_match_tags": "{\"amenity\": \"parcel_locker\", \"brand\": \"Allegro One Box\", \"brand:wikidata\": \"Q110738715\"}",
    "osm_element_match_id": null,
    "osm_element_match_type": null,
    "match_distance": null,
    "all_very_good_matches": "null",
    "report_type": "OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP",
    "is_marked_as_bad_by_mapper": 0
  }
]""")
        val atpEntries = listOf(
            AtpEntry(
                position = LatLon(50.337333, 19.926502),
                id = -3097502835224812,
                osmMatch = null,
                tagsInATP = mapOf(
                    "ref" to "AL004KMI",
                    "description" to "Na placu",
                    "@source_uri" to "https://edge.allegro.pl/general-deliveries/2595554",
                    "@spider" to "allegro_one_box_pl",
                    "amenity" to "parcel_locker",
                    "addr:country" to "PL",
                    "website" to "https://allegro.pl/kampania/one/znajdz-nas?pointId=2595554",
                    "opening_hours" to "24/7",
                    "brand" to "Allegro One Box",
                    "brand:wikidata" to "Q110738715",
                    "atp_id" to "joZ8fm6HJhR0l2oH6b9TgijR_EI=",
                    "atp_ref" to "AL004KMI",
                ),
                tagsInOSM = null,
                reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP
            ),
            AtpEntry(
                position = LatLon(50.283805, 19.920721),
                id = 1834581365633738,
                osmMatch = null, // ElementKey(ElementType.WAY, 403376332), // or similar expected TODO wait for API to support it
                tagsInATP = mapOf(
                    "ref" to "AL005KMI",
                    "description" to "Na placu",
                    "@source_uri" to "https://edge.allegro.pl/general-deliveries/2593315",
                    "@spider" to "allegro_one_box_pl",
                    "amenity" to "parcel_locker",
                    "addr:country" to "PL",
                    "website" to "https://allegro.pl/kampania/one/znajdz-nas?pointId=2593315",
                    "opening_hours" to "24/7",
                    "brand" to "Allegro One Box",
                    "brand:wikidata" to "Q110738715",
                    "atp_id" to "G5H5NoXhe_2QjVhvZt9LjxzJ6sE=",
                    "atp_ref" to "AL005KMI",
                ),
                tagsInOSM = mapOf(
                    "amenity" to "parcel_locker",
                    "brand" to "Allegro One Box",
                    "brand:wikidata" to "Q110738715",
                ),
                reportType = ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP
            ),
        )

        assertEquals(atpEntries, AtpApiParser().parseAtpEntries(buffer))
    }

}
