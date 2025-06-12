package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.contains
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/** Takes care of downloading ATP data into persistent storage */
class AtpDownloader(
    private val atpApi: AtpApiClient,
    private val atpController: AtpController
) {
    suspend fun download(bbox: BoundingBox) {
        val time = nowAsEpochMilliseconds()

        val entries: Collection<AtpEntry> = hardcodedList()

        /*
        val entries = notesApi // TODO look at notesApi, create ATP API
            .getAllOpen(bbox, 10000)
            // exclude invalid notes (#1338)
            .filter { it.comments.isNotEmpty() }
        */
        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
        Log.i(TAG, "Downloaded ${entries.size} ATP entries in ${seconds.format(1)}s")

        yield()

        withContext(Dispatchers.IO) { atpController.putAllForBBox(bbox, entries) }
    }

    private fun hardcodedList(): List<AtpEntry> {
        return listOf(
            AtpEntry(
                position = LatLon(latitude=50.0819622, longitude=19.9997371),
                id = 1,
                osmMatch = ElementKey(ElementType.NODE, 5922786967),
                tagsInATP = mapOf("opening_hours" to "Mo-We 08:00-21:30; Fr 08:00-21:30; Su 10:00-20:00", "shop" to "chemist", "name" to "Rossmann", "brand" to "Rossmann", "brand:wikidata" to "Q316004", "website" to "https://www.rossmann.pl/drogerie/Drogeria-Rossmann-boleslawa-orlinskiego-1,krakow,12,1536"),
                tagsInOSM = mapOf("brand" to "Rossmann", "brand:wikidata" to "Q316004", "brand:wikipedia" to "pl:Rossmann", "name" to "Rossmann", "opening_hours" to "Mo-Fr 08:30-21:30; Sa 08:30-21:30; Su 10:00-20:00", "shop" to "chemist", "wheelchair" to "yes"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.077953, longitude=19.970775),
                id = 2,
                osmMatch = ElementKey(ElementType.NODE, 5429751728),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID04981-krakow-pilotow-29-lok-u1/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; Su 12:00-20:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.070186, longitude=19.976718),
                id = 3,
                osmMatch = ElementKey(ElementType.NODE, 5271986725),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID05132-krakow-ostatnia-1f-lok-u1-i-1g-lok-u1/"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:country" to "PL", "addr:housenumber" to "1g", "addr:postcode" to "31-444", "addr:street" to "Ostatnia", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; Su 10:00-20:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/", "wheelchair" to "limited"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.091623, longitude=19.979187),
                id = 4,
                osmMatch = ElementKey(ElementType.NODE, 5432092155),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-14:00; Su 00:00-24:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID05780-krakow-kwartowa-16-lok-u1-i-u2/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "check_date:opening_hours" to "2024-04-08", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.074605, longitude=19.994264),
                id = 5,
                osmMatch = ElementKey(ElementType.NODE, 9220814763),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-22:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID05856-krakow-jana-pawla-ii-31-lok-u3a/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Su 06:00-22:00", "shop" to "convenience"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.073126, longitude=19.982864),
                id = 6,
                osmMatch = ElementKey(ElementType.NODE, 6736780900),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID07072-krakow-ul-lakowa-19/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; Su 08:00-23:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/", "wheelchair" to "limited"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.095455, longitude=19.989608),
                id = 7,
                osmMatch = ElementKey(ElementType.NODE, 10061939049),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID07691-krakow-ul-kurzei-2-lok-u1/"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:housenumber" to "2", "addr:postcode" to "31-618", "addr:street" to "Księdza Józefa Kurzei", "name" to "Żabka", "opening_hours" to "Mo-Su 06:00-23:00", "payment:app" to "yes", "payment:cash" to "yes", "payment:credit_cards" to "yes", "payment:mastercard" to "yes", "payment:telephone_cards" to "yes", "payment:visa" to "yes", "shop" to "supermarket"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.083934, longitude=19.998491),
                id = 8,
                osmMatch = ElementKey(ElementType.NODE, 2360392546),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 00:00-24:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID09063-krakow-ul-stanislawa-skarzynskiego-6/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; Su 11:00-20:00", "shop" to "convenience"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.072679, longitude=19.980586),
                id = 9,
                osmMatch = ElementKey(ElementType.NODE, 8070981385),
                tagsInATP = mapOf("opening_hours" to "Mo-Su 10:00-20:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID09246-krakow-ul-meissnera-32/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.092712, longitude=19.974625),
                id = 10,
                osmMatch = ElementKey(ElementType.NODE, 12175643244),
                tagsInATP = mapOf("opening_hours" to "Mo-Su 09:00-22:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID0E106-krakow-ul-strzelcow-11b/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.089821, longitude=19.974156),
                id = 11,
                osmMatch = ElementKey(ElementType.NODE, 12199560862),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 00:00-24:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID0E183-krakow-ul-strzelcow-2/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.077007, longitude=20.037982),
                id = 12,
                osmMatch = ElementKey(ElementType.NODE, 7966024085),
                tagsInATP = mapOf("opening_hours" to "Mo-Fr 08:00-21:00; Sa 08:00-16:00", "amenity" to "pharmacy", "name" to "Ziko Apteka", "brand" to "Ziko Apteka", "brand:wikidata" to "Q63432892", "website" to "https://zikoapteka.pl/apteki/krakow/urocze-1/"),
                tagsInOSM = mapOf("amenity" to "pharmacy", "brand" to "Ziko Apteka", "brand:wikidata" to "Q63432892", "check_date:opening_hours" to "2025-03-21", "dispensing" to "yes", "healthcare" to "pharmacy", "name" to "Ziko Apteka", "opening_hours" to "Mo-Fr 07:00-20:00; Sa 08:00-16:00", "ref:csioz" to "1035547", "short_name" to "Ziko", "trash_accepted:medicines" to "no"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.071739, longitude=20.038376),
                id = 13,
                osmMatch = ElementKey(ElementType.NODE, 1725369246),
                tagsInATP = mapOf("opening_hours" to "Mo-We 08:00-21:00; Fr 08:00-21:00; Su 10:00-18:00", "shop" to "chemist", "name" to "Rossmann", "brand" to "Rossmann", "brand:wikidata" to "Q316004", "website" to "https://www.rossmann.pl/drogerie/Drogeria-Rossmann-centrum-a1,krakow,12,663"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:country" to "PL", "addr:housenumber" to "1", "addr:place" to "Osiedle Centrum A", "brand" to "Rossmann", "brand:wikidata" to "Q316004", "brand:wikipedia" to "pl:Rossmann", "name" to "Rossmann", "opening_hours" to "Mo-Fr 08:00-21:00; Sa 08:30-20:30", "shop" to "chemist", "wheelchair" to "limited"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.09394733, longitude=20.00223635),
                id = 14,
                osmMatch = ElementKey(ElementType.NODE, 4872347125),
                tagsInATP = mapOf("opening_hours" to "Mo-We 08:00-21:30; Fr 08:00-21:30; Su 10:00-18:00", "shop" to "chemist", "name" to "Rossmann", "brand" to "Rossmann", "brand:wikidata" to "Q316004", "website" to "https://www.rossmann.pl/drogerie/Drogeria-Rossmann-tysiaclecia-42,krakow,12,1235"),
                tagsInOSM = mapOf("brand" to "Rossmann", "brand:wikidata" to "Q316004", "brand:wikipedia" to "pl:Rossmann", "name" to "Rossmann", "opening_hours" to "Mo-Fr 08:00-21:00; Sa 08:00-20:30", "shop" to "chemist"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.085785, longitude=20.018721),
                id = 15,
                osmMatch = ElementKey(ElementType.NODE, 5233230802),
                tagsInATP = mapOf("opening_hours" to "Mo-We 09:00-20:00; Fr 09:00-20:00", "shop" to "chemist", "name" to "Rossmann", "brand" to "Rossmann", "brand:wikidata" to "Q316004", "website" to "https://www.rossmann.pl/drogerie/Drogeria-Rossmann-broniewskiego-1,krakow,12,340"),
                tagsInOSM = mapOf("brand" to "Rossmann", "brand:wikidata" to "Q316004", "brand:wikipedia" to "pl:Rossmann", "name" to "Rossmann", "opening_hours" to "Mo-Fr 09:00-20:00; Sa 09:00-15:00", "shop" to "chemist"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.073259, longitude=20.04192),
                id = 16,
                osmMatch = ElementKey(ElementType.NODE, 4426462366),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 09:00-21:00; Su 00:00-24:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID02724-krakow-os-centrum-a3-lok-u-1/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; Su 09:00-21:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.086261, longitude=20.014516),
                id = 17,
                osmMatch = ElementKey(ElementType.NODE, 3043489972),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 09:00-21:00; Su 00:00-24:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID02880-krakow-os-na-lotnisku-10/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.073226, longitude=20.037421),
                id = 18,
                osmMatch = ElementKey(ElementType.NODE, 4271289403),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-22:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID03723-krakow-centrum-c-1-u5/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-22:00; Su 09:00-20:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/", "wheelchair" to "yes"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.079074, longitude=20.015939),
                id = 19,
                osmMatch = ElementKey(ElementType.NODE, 5431937957),
                tagsInATP = mapOf("opening_hours" to "Mo-Su 06:00-18:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID04185-krakow-marii-dabrowskiej-17-lok-66/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "check_date:opening_hours" to "2024-08-23", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-22:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.096381, longitude=20.016879),
                id = 20,
                osmMatch = ElementKey(ElementType.NODE, 3745503635),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 11:00-21:00; Su 00:00-24:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID05239-krakow-piastow-12m-lok-lu-u1/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Su 06:00-23:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.073598, longitude=20.02508),
                id = 21,
                osmMatch = ElementKey(ElementType.NODE, 2372246441),
                tagsInATP = mapOf("opening_hours" to "Mo-Su 10:00-20:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID05551-krakow-kolorowe-16b/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "check_date:opening_hours" to "2025-01-08", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; Su 10:00-20:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/", "wheelchair" to "no"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.075162, longitude=20.035524),
                id = 22,
                osmMatch = ElementKey(ElementType.NODE, 11174714711),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID05700-krakow-centrum-c-6-lok-u002/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "check_date:opening_hours" to "2025-03-31", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Fr 06:00-23:00; Su 11:00-20:00", "shop" to "convenience", "wheelchair" to "limited"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.090758, longitude=20.018906),
                id = 23,
                osmMatch = ElementKey(ElementType.NODE, 9072253368),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 09:00-21:00; Su 00:00-24:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID05983-krakow-wysokie-22a/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00", "shop" to "convenience"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.081394, longitude=20.001182),
                id = 24,
                osmMatch = ElementKey(ElementType.NODE, 5371065002),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 10:00-22:00; Su 11:00-21:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID05999-krakow-orlinskiego-3-lok-u3/"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:housenumber" to "3", "addr:postcode" to "31-878", "addr:street" to "Bolesława Orlińskiego", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; Su 10:00-22:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/", "wheelchair" to "yes"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.09839, longitude=20.013997),
                id = 25,
                osmMatch = ElementKey(ElementType.NODE, 6436938492),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-22:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID06598-krakow-os-piastow-41/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-22:00; Su 10:00-20:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.075976, longitude=20.032573),
                id = 26,
                osmMatch = ElementKey(ElementType.NODE, 6524308045),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 07:00-21:00; Su 00:00-24:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID06609-krakow-os-zgody-3-lok-u-2/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "diet:halal" to "no", "diet:kosher" to "no", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-22:00; Su 09:00-21:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/", "wheelchair" to "limited"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.092991, longitude=20.022102),
                id = 27,
                osmMatch = ElementKey(ElementType.NODE, 3650373570),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 09:00-20:00; Su 00:00-24:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID08071-krakow-ul-okulickiego-51-l287/"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:housenumber" to "51", "addr:postcode" to "31-637", "addr:street" to "Generała Leopolda Okulickiego", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; Su 09:00-22:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://www.zabka.pl/znajdz-sklep/ID08071-krakow-ul-okulickiego-51-l287/", "wheelchair" to "yes"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.083808, longitude=20.018456),
                id = 28,
                osmMatch = ElementKey(ElementType.NODE, 8111668837),
                tagsInATP = mapOf("opening_hours" to "Mo-Su 11:00-20:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID08970-krakow-os-albertynskie-21b/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; Su 10:00-20:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.084958, longitude=20.020944),
                id = 29,
                osmMatch = ElementKey(ElementType.NODE, 11283874924),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 11:00-20:00; Su 00:00-24:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID09300-krakow-os-kazimierzowskie-35b/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00", "shop" to "convenience"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.098812, longitude=20.009587),
                id = 30,
                osmMatch = ElementKey(ElementType.NODE, 11612210728),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 10:00-20:00; Su 00:00-24:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID0D012-krakow-ul-os-bohaterow-wrzesnia-39d/"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:housenumber" to "39", "addr:postcode" to "31-621", "addr:street" to "Osiedle Bohaterów Września", "name" to "Żabka", "opening_hours" to "Mo-Su 06:00-23:00", "payment:apple_pay" to "yes", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:credit_cards" to "yes", "payment:google_pay" to "yes", "payment:notes" to "yes", "shop" to "convenience"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.084428, longitude=20.027786),
                id = 31,
                osmMatch = ElementKey(ElementType.NODE, 9059459532),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 00:00-24:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID0D380-krakow-os-os-jagiellonskie-19/"),
                tagsInOSM = mapOf("atm" to "yes", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "check_date" to "2024-03-23", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Su 06:00-23:00", "payment:blik" to "yes", "payment:cards" to "yes", "payment:cash" to "yes", "payment:contactless" to "yes", "shop" to "convenience"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            )
            ,
            AtpEntry(
                position = LatLon(latitude=50.08688540000001, longitude=19.9859261),
                id = 32,
                osmMatch = null,
                tagsInATP = mapOf("shop" to "cosmetics", "name" to "Rituals", "brand" to "Rituals", "brand:wikidata" to "Q62874140", "website" to "https://www.rituals.com/pl-pl/store-detail?store=Krak%C3%B3w-Centrum-Serenada"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0875, longitude=19.98727),
                id = 33,
                osmMatch = ElementKey(ElementType.WAY, 1074322018),
                tagsInATP = mapOf("amenity" to "cafe", "name" to "Starbucks", "brand" to "Starbucks", "brand:wikidata" to "Q37158", "website" to "https://www.starbucks.co.uk/store-locator/51856-261544/krakow,-serenada"),
                tagsInOSM = mapOf("amenity" to "fast_food", "building" to "kiosk", "building:levels" to "1", "outdoor_seating" to "yes"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.087761, longitude=19.985448),
                id = 34,
                osmMatch = ElementKey(ElementType.WAY, 1359385923),
                tagsInATP = mapOf("shop" to "chemist", "name" to "Rossmann", "brand" to "Rossmann", "brand:wikidata" to "Q316004", "website" to "https://www.rossmann.pl/drogerie/Drogeria-Rossmann-gen-t-bora-komorowskiego-41,krakow,12,1163"),
                tagsInOSM = mapOf("brand" to "Rossmann", "brand:wikidata" to "Q316004", "brand:wikipedia" to "pl:Rossmann", "check_date:opening_hours" to "2023-08-16", "indoor" to "room", "level" to "0", "name" to "Rossmann", "opening_hours" to "Mo-Sa 08:30-21:30", "shop" to "chemist", "website" to "https://www.rossmann.pl/", "wheelchair" to "yes"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0882, longitude=19.98595),
                id = 35,
                osmMatch = null,
                tagsInATP = mapOf("shop" to "books", "name" to "Empik", "brand" to "Empik", "brand:wikidata" to "Q3045978", "website" to "https://www.empik.com/salony-empik/krakow/krakow-serenada-sp,35988,e"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.093882, longitude=19.989662),
                id = 36,
                osmMatch = ElementKey(ElementType.NODE, 5871989987),
                tagsInATP = mapOf("shop" to "convenience", "name" to "Groszek", "brand" to "Groszek", "brand:wikidata" to "Q9280965"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:country" to "PL", "addr:housenumber" to "76", "addr:place" to "Osiedle Tysiąclecia", "shop" to "convenience"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0804135, longitude=19.9926123),
                id = 37,
                osmMatch = null,
                tagsInATP = mapOf("amenity" to "bank", "name" to "Alior Bank", "brand" to "Alior Bank", "brand:wikidata" to "Q9148395"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0804135, longitude=19.9926123),
                id = 38,
                osmMatch = null,
                tagsInATP = mapOf("amenity" to "bank", "name" to "Alior Bank", "brand" to "Alior Bank", "brand:wikidata" to "Q9148395"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.095042, longitude=20.011993),
                id = 39,
                osmMatch = null,
                tagsInATP = mapOf("amenity" to "bank", "name" to "Alior Bank", "brand" to "Alior Bank", "brand:wikidata" to "Q9148395"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.087255, longitude=19.975505),
                id = 40,
                osmMatch = ElementKey(ElementType.NODE, 12281969301),
                tagsInATP = mapOf("shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID0A815-krakow-ul-bora-komorowskiego-25d/"),
                tagsInOSM = mapOf("name" to "Żabka Nano", "opening_hours" to "24/7", "shop" to "convenience"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.091498, longitude=19.987376),
                id = 41,
                osmMatch = ElementKey(ElementType.NODE, 10067213453),
                tagsInATP = mapOf("shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID0E588-krakow-ul-bohomolca-13-lok-lu-1/"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:housenumber" to "53A", "addr:postcode" to "31-416", "addr:street" to "Marchołta", "name" to "Żabka", "opening_hours" to "Mo-Su 06:00-23:00", "payment:cards" to "yes", "payment:cash" to "yes", "payment:credit_cards" to "yes", "payment:debit_cards" to "yes", "payment:mastercard" to "yes", "payment:telephone_cards" to "yes", "payment:visa" to "yes", "shop" to "supermarket"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.092896, longitude=20.014811),
                id = 42,
                osmMatch = ElementKey(ElementType.NODE, 9044149328),
                tagsInATP = mapOf("shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID02220-krakow-ul-mikolajczyka-os-kalinowe-12c/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "check_date:opening_hours" to "2024-11-18", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; PH,Su 10:00-21:00", "opening_hours:signed" to "no", "shop" to "convenience"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.080458, longitude=20.003712),
                id = 43,
                osmMatch = ElementKey(ElementType.NODE, 9168905169),
                tagsInATP = mapOf("shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID06239-krakow-os-avia-3-lok-u1/"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:housenumber" to "3", "addr:place" to "Osiedle Avia", "addr:postcode" to "31-877", "addr:unit" to "U1", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; Su,PH 11:00-19:00", "shop" to "convenience"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.088771, longitude=19.986762),
                id = 44,
                osmMatch = ElementKey(ElementType.NODE, 9021402707),
                tagsInATP = mapOf("shop" to "clothes", "name" to "H&M", "brand" to "H&M", "brand:wikidata" to "Q188326"),
                tagsInOSM = mapOf("shop" to "clothes"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.09148, longitude=19.9885),
                id = 45,
                osmMatch = null,
                tagsInATP = mapOf("shop" to "frozen_food", "name" to "Wesoła Pani", "brand" to "Wesoła Pani", "brand:wikidata" to "Q123240454"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.08179, longitude=20.00081),
                id = 46,
                osmMatch = null,
                tagsInATP = mapOf("shop" to "frozen_food", "name" to "Wesoła Pani", "brand" to "Wesoła Pani", "brand:wikidata" to "Q123240454"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07324, longitude=20.02859),
                id = 47,
                osmMatch = ElementKey(ElementType.NODE, 9984802330),
                tagsInATP = mapOf("shop" to "frozen_food", "name" to "Wesoła Pani", "brand" to "Wesoła Pani", "brand:wikidata" to "Q123240454"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:housenumber" to "2", "addr:postcode" to "31-870", "addr:street" to "Mieczysława Medweckiego", "food" to "prepared_meals", "level" to "0", "opening_hours" to "Mo-Sa 09:00-21:00; Su 10:00-20:00", "shop" to "food"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.087643, longitude=19.985059),
                id = 48,
                osmMatch = null,
                tagsInATP = mapOf("shop" to "clothes", "name" to "Pepco", "brand" to "Pepco", "brand:wikidata" to "Q11815580"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0884449, longitude=19.9942296),
                id = 49,
                osmMatch = ElementKey(ElementType.NODE, 5871989987),
                tagsInATP = mapOf("shop" to "convenience", "name" to "Odido", "brand" to "Odido", "brand:wikidata" to "Q106947294", "website" to "https://www.sklepy-odido.pl/znajdz-sklep/osiedle-oswiecenia-21"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:country" to "PL", "addr:housenumber" to "76", "addr:place" to "Osiedle Tysiąclecia", "shop" to "convenience"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0833298, longitude=20.0168437),
                id = 50,
                osmMatch = null,
                tagsInATP = mapOf("shop" to "convenience", "name" to "Odido", "brand" to "Odido", "brand:wikidata" to "Q106947294", "website" to "https://www.sklepy-odido.pl/znajdz-sklep/kosciuszkowskie-6a"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0819207, longitude=20.011011),
                id = 51,
                osmMatch = null,
                tagsInATP = mapOf("amenity" to "post_office", "name" to "Epaka.pl", "brand" to "Epaka.pl", "brand:wikidata" to "Q123028724", "website" to "https://krakow-dywizjonu303.epaka.pl"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.101585, longitude=20.013522),
                id = 52,
                osmMatch = ElementKey(ElementType.NODE, 1819649746),
                tagsInATP = mapOf("shop" to "supermarket", "name" to "Lewiatan", "brand" to "Lewiatan", "brand:wikidata" to "Q11755396", "website" to "https://www.lewiatan.pl/znajdz-sklep/lewiatan-krakow-os-piastow-60"),
                tagsInOSM = mapOf("shop" to "convenience"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.073014, longitude=20.036947),
                id = 53,
                osmMatch = ElementKey(ElementType.NODE, 4271289402),
                tagsInATP = mapOf("amenity" to "pharmacy", "name" to "Ziko Apteka", "brand" to "Ziko Apteka", "brand:wikidata" to "Q63432892", "website" to "https://zikoapteka.pl/apteki/krakow/centrum-c1/"),
                tagsInOSM = mapOf("amenity" to "pharmacy", "brand" to "Ziko Apteka", "brand:wikidata" to "Q63432892", "check_date:opening_hours" to "2024-09-25", "dispensing" to "yes", "healthcare" to "pharmacy", "name" to "Ziko Apteka", "opening_hours" to "Mo-Fr 08:00-21:00, Sa 08:00-16:00", "payment:cash" to "yes", "payment:coins" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "phone" to "+48 12 687 57 47", "ref:csioz" to "1188707", "short_name" to "Ziko", "trash_accepted:medicines" to "yes", "website" to "https://www.ziko.pl/krakow-apteki-drogerie-ziko/centrum-c1/", "wheelchair" to "yes"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.084492, longitude=20.028099),
                id = 54,
                osmMatch = ElementKey(ElementType.NODE, 6375904177),
                tagsInATP = mapOf("amenity" to "pharmacy", "name" to "Ziko Apteka", "brand" to "Ziko Apteka", "brand:wikidata" to "Q63432892", "website" to "https://zikoapteka.pl/apteki/krakow/jagiellonskie-19/"),
                tagsInOSM = mapOf("amenity" to "pharmacy", "brand" to "Ziko Apteka", "brand:wikidata" to "Q63432892", "dispensing" to "yes", "healthcare" to "pharmacy", "name" to "Ziko Apteka", "opening_hours" to "Mo-Fr 08:00-20:00; Sa 08:00-16:00", "payment:cash" to "yes", "payment:coins" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "phone" to "+48 12 687 57 47", "ref:csioz" to "1034564", "short_name" to "Ziko", "website" to "https://www.ziko.pl/krakow-apteki-drogerie-ziko/jagiellonskie-19/"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            )
        )
    }

    companion object {
        private const val TAG = "AtpDownload"
    }
}
