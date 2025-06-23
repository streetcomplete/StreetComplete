import de.westnordost.streetcomplete.data.atp.AtpApiParser
import de.westnordost.streetcomplete.data.atp.AtpEntry
import de.westnordost.streetcomplete.data.atp.ReportType
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
        buffer.writeString("""    {
    "type": "FeatureCollection",
    "features": []
    }""")
        assertEquals(listOf(), AtpApiParser().parseAtpEntries(buffer))
    }

    @Test
    fun `parse one minimum atp entry`() {
        val buffer = Buffer()
        buffer.writeString("""{
    "type": "FeatureCollection",
    "features": [
        {
            "type": "Feature",
            "geometry": {
                "type": "Point",
                "coordinates": [
                    24.01383,
                    50.49832
                ]
            },
            "properties": {
                "atp_center": {
                    "lat": 50.49832,
                    "lon": 24.01383
                },
                "atp_tags": {
                    "amenity": "parcel_locker",
                    "brand": "Paczkomat InPost",
                    "brand:wikidata": "Q110970254"
                },
                "osm_match_tags": null,
                "osm_object_type": null,
                "osm_object_id": null,
                "osm_link": null,
                "error_type": "MISSING_POI_IN_OPENSTREETMAP",
                "entry_id": -1504970035065324465
            }
        }
    ]
}"""
        )

        val atpEntry = AtpEntry(
            position = LatLon(50.49832, 24.01383),
            id = -1504970035065324465,
            osmMatch = null,
            tagsInATP = mapOf(
                "amenity" to "parcel_locker",
                "brand" to "Paczkomat InPost",
                "brand:wikidata" to "Q110970254"
            ),
            tagsInOSM = null,
            reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP
        )

        assertEquals(listOf(atpEntry), AtpApiParser().parseAtpEntries(buffer))
    }

    @Test
    fun `parse two more complete atp entries`() {
        val buffer = Buffer()
        buffer.writeString("""{
    "type": "FeatureCollection",
    "features": [
        {
            "type": "Feature",
            "geometry": {
                "type": "Point",
                "coordinates": [
                    149.568428,
                    -33.418319
                ]
            },
            "properties": {
                "atp_center": {
                    "lat": -33.418319,
                    "lon": 149.568428
                },
                "atp_tags": {
                    "opening_hours": "Mo-Sa 09:00-18:00",
                    "amenity": "pharmacy",
                    "name": "TerryWhite Chemmart",
                    "brand": "TerryWhite Chemmart",
                    "brand:wikidata": "Q24089773"
                },
                "osm_match_tags": {
                    "amenity": "pharmacy",
                    "brand": "TerryWhite Chemmart",
                    "brand:wikidata": "Q24089773",
                    "building": "yes",
                    "dispensing": "yes",
                    "healthcare": "pharmacy",
                    "name": "TerryWhite Chemmart",
                    "opening_hours": "Mo-Sa 08:00-18:00"
                },
                "osm_object_type": "way",
                "osm_object_id": "403376332",
                "osm_link": "https://www.openstreetmap.org/way/403376332",
                "error_type": "OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP",
                "entry_id": -4038337681667840018
            }
        },
        {
            "type": "Feature",
            "geometry": {
                "type": "Point",
                "coordinates": [
                    149.07415,
                    -33.267848
                ]
            },
            "properties": {
                "atp_center": {
                    "lat": -33.267848,
                    "lon": 149.07415
                },
                "atp_tags": {
                    "amenity": "post_box",
                    "brand": "Australia Post",
                    "brand:wikidata": "Q1142936"
                },
                "osm_match_tags": null,
                "osm_object_type": null,
                "osm_object_id": null,
                "osm_link": null,
                "error_type": "MISSING_POI_IN_OPENSTREETMAP",
                "entry_id": 566686446681342682
            }
        }
    ]
}""")
        val atpEntries = listOf(
            AtpEntry(
                position = LatLon(-33.418319, 149.568428),
                id = -4038337681667840018,
                osmMatch = ElementKey(ElementType.WAY, 403376332),
                tagsInATP = mapOf(
                    "opening_hours" to "Mo-Sa 09:00-18:00",
                    "amenity" to "pharmacy",
                    "name" to "TerryWhite Chemmart",
                    "brand" to "TerryWhite Chemmart",
                    "brand:wikidata" to "Q24089773"
                ),
                tagsInOSM = mapOf(
                    "amenity" to "pharmacy",
                    "brand" to "TerryWhite Chemmart",
                    "brand:wikidata" to "Q24089773",
                    "building" to "yes",
                    "dispensing" to "yes",
                    "healthcare" to "pharmacy",
                    "name" to "TerryWhite Chemmart",
                    "opening_hours" to "Mo-Sa 08:00-18:00"
                ),
                reportType = ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP
            ),
            AtpEntry(
                position = LatLon(-33.267848, 149.07415),
                id = 566686446681342682,
                osmMatch = null,
                tagsInATP = mapOf(
                    "amenity" to "post_box",
                    "brand" to "Australia Post",
                    "brand:wikidata" to "Q1142936"
                ),
                tagsInOSM = null,
                reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP
            )
        )

        assertEquals(atpEntries, AtpApiParser().parseAtpEntries(buffer))
    }

}
