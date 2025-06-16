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
                position = LatLon(latitude=50.090275, longitude=19.976023),
                id = 1,
                osmMatch = ElementKey(ElementType.NODE, 9248004024),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 11:00-20:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID02053-krakow-ul-sloneckiego-4-nr-po90/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.077953, longitude=19.970775),
                id = 2,
                osmMatch = ElementKey(ElementType.NODE, 5429751728),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 11:00-21:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID04981-krakow-pilotow-29-lok-u1/"),
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
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 11:00-20:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID05780-krakow-kwartowa-16-lok-u1-i-u2/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "check_date:opening_hours" to "2024-04-08", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.074605, longitude=19.994264),
                id = 5,
                osmMatch = ElementKey(ElementType.NODE, 9220814763),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-22:00; Su 10:00-20:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID05856-krakow-jana-pawla-ii-31-lok-u3a/"),
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
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 10:00-22:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID07691-krakow-ul-kurzei-2-lok-u1/"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:housenumber" to "2", "addr:postcode" to "31-618", "addr:street" to "Księdza Józefa Kurzei", "name" to "Żabka", "opening_hours" to "Mo-Su 06:00-23:00", "payment:app" to "yes", "payment:cash" to "yes", "payment:credit_cards" to "yes", "payment:mastercard" to "yes", "payment:telephone_cards" to "yes", "payment:visa" to "yes", "shop" to "supermarket"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.073045, longitude=19.975487),
                id = 8,
                osmMatch = ElementKey(ElementType.NODE, 10697790005),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 09:00-22:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID08560-krakow-sliczna-34b-u1/"),
                tagsInOSM = mapOf("name" to "Żabka", "opening_hours" to "Mo-Sa 06:00-23:00", "shop" to "convenience"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.083934, longitude=19.998491),
                id = 9,
                osmMatch = ElementKey(ElementType.NODE, 2360392546),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID09063-krakow-ul-stanislawa-skarzynskiego-6/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; Su 11:00-20:00", "shop" to "convenience"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.072679, longitude=19.980586),
                id = 10,
                osmMatch = ElementKey(ElementType.NODE, 8070981385),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 10:00-20:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID09246-krakow-ul-meissnera-32/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.089821, longitude=19.974156),
                id = 11,
                osmMatch = ElementKey(ElementType.NODE, 12199560862),
                tagsInATP = mapOf("opening_hours" to "Mo-Su 06:00-23:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID0E183-krakow-ul-strzelcow-2/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.071825, longitude=20.038465),
                id = 12,
                osmMatch = ElementKey(ElementType.NODE, 1725369246),
                tagsInATP = mapOf("opening_hours" to "Mo-Fr 08:00-21:00; Sa 08:00-20:30", "shop" to "chemist", "name" to "Rossmann", "brand" to "Rossmann", "brand:wikidata" to "Q316004", "website" to "https://www.rossmann.pl/drogerie/Drogeria-Rossmann-centrum-a1,krakow,12,663"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:country" to "PL", "addr:housenumber" to "1", "addr:place" to "Osiedle Centrum A", "brand" to "Rossmann", "brand:wikidata" to "Q316004", "brand:wikipedia" to "pl:Rossmann", "name" to "Rossmann", "opening_hours" to "Mo-Fr 08:00-21:00; Sa 08:30-20:30", "shop" to "chemist", "wheelchair" to "limited"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.09393, longitude=20.00229),
                id = 13,
                osmMatch = ElementKey(ElementType.NODE, 4872347125),
                tagsInATP = mapOf("opening_hours" to "Mo-Fr 08:00-21:30; Sa 08:00-20:30", "shop" to "chemist", "name" to "Rossmann", "brand" to "Rossmann", "brand:wikidata" to "Q316004", "website" to "https://www.rossmann.pl/drogerie/Drogeria-Rossmann-tysiaclecia-42,krakow,12,1235"),
                tagsInOSM = mapOf("brand" to "Rossmann", "brand:wikidata" to "Q316004", "brand:wikipedia" to "pl:Rossmann", "name" to "Rossmann", "opening_hours" to "Mo-Fr 08:00-21:00; Sa 08:00-20:30", "shop" to "chemist"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07643, longitude=20.01626),
                id = 14,
                osmMatch = ElementKey(ElementType.WAY, 1089809204),
                tagsInATP = mapOf("opening_hours" to "Mo-Fr 08:30-21:30; Sa 08:30-22:00", "shop" to "chemist", "name" to "Rossmann", "brand" to "Rossmann", "brand:wikidata" to "Q316004", "website" to "https://www.rossmann.pl/drogerie/Drogeria-Rossmann-medweckiego-2,krakow,12,1125"),
                tagsInOSM = mapOf("access" to "customers", "addr:city" to "Kraków", "addr:housenumber" to "2", "addr:postcode" to "31-870", "addr:street" to "Mieczysława Medweckiego", "brand" to "Rossmann", "brand:wikidata" to "Q316004", "check_date:opening_hours" to "2024-11-27", "indoor" to "room", "level" to "0", "name" to "Rossmann", "opening_hours" to "Mo-Sa 08:30-21:00; Su 10:00-20:00", "opening_hours:signed" to "no", "room" to "shop", "shop" to "chemist"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.077007, longitude=20.037982),
                id = 15,
                osmMatch = ElementKey(ElementType.NODE, 7966024085),
                tagsInATP = mapOf("opening_hours" to "Mo-Fr 08:00-21:00; Sa 08:00-16:00", "amenity" to "pharmacy", "name" to "Ziko Apteka", "brand" to "Ziko Apteka", "brand:wikidata" to "Q63432892", "website" to "https://zikoapteka.pl/apteki/krakow/urocze-1/"),
                tagsInOSM = mapOf("amenity" to "pharmacy", "brand" to "Ziko Apteka", "brand:wikidata" to "Q63432892", "check_date:opening_hours" to "2025-03-21", "dispensing" to "yes", "healthcare" to "pharmacy", "name" to "Ziko Apteka", "opening_hours" to "Mo-Fr 07:00-20:00; Sa 08:00-16:00", "ref:csioz" to "1035547", "short_name" to "Ziko", "trash_accepted:medicines" to "no"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.073259, longitude=20.04192),
                id = 16,
                osmMatch = ElementKey(ElementType.NODE, 4426462366),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 08:00-21:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID02724-krakow-os-centrum-a3-lok-u-1/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; Su 09:00-21:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.086261, longitude=20.014516),
                id = 17,
                osmMatch = ElementKey(ElementType.NODE, 3043489972),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 09:00-20:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID02880-krakow-os-na-lotnisku-10/"),
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
                position = LatLon(latitude=50.073598, longitude=20.02508),
                id = 19,
                osmMatch = ElementKey(ElementType.NODE, 2372246441),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 10:00-21:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID05551-krakow-kolorowe-16b/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "check_date:opening_hours" to "2025-01-08", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; Su 10:00-20:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/", "wheelchair" to "no"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.075162, longitude=20.035524),
                id = 20,
                osmMatch = ElementKey(ElementType.NODE, 11174714711),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 11:00-20:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID05700-krakow-centrum-c-6-lok-u002/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "check_date:opening_hours" to "2025-03-31", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Fr 06:00-23:00; Su 11:00-20:00", "shop" to "convenience", "wheelchair" to "limited"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.09839, longitude=20.013997),
                id = 21,
                osmMatch = ElementKey(ElementType.NODE, 6436938492),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-22:00; Su 06:00-21:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID06598-krakow-os-piastow-41/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-22:00; Su 10:00-20:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.075976, longitude=20.032573),
                id = 22,
                osmMatch = ElementKey(ElementType.NODE, 6524308045),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-22:00; Su 07:00-21:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID06609-krakow-os-zgody-3-lok-u-2/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "diet:halal" to "no", "diet:kosher" to "no", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-22:00; Su 09:00-21:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://zabka.pl/", "wheelchair" to "limited"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.092991, longitude=20.022102),
                id = 23,
                osmMatch = ElementKey(ElementType.NODE, 3650373570),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 09:00-20:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID08071-krakow-ul-okulickiego-51-l287/"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:housenumber" to "51", "addr:postcode" to "31-637", "addr:street" to "Generała Leopolda Okulickiego", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; Su 09:00-22:00", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:contactless" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "shop" to "convenience", "website" to "https://www.zabka.pl/znajdz-sklep/ID08071-krakow-ul-okulickiego-51-l287/", "wheelchair" to "yes"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.084958, longitude=20.020944),
                id = 24,
                osmMatch = ElementKey(ElementType.NODE, 11283874924),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 10:00-20:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID09300-krakow-os-kazimierzowskie-35b/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; Su 11:00-20:00", "shop" to "convenience", "wheelchair" to "limited"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.081202, longitude=20.047556),
                id = 25,
                osmMatch = ElementKey(ElementType.NODE, 12023026069),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID0C347-krakow-ul-bulwarowa-35-lok-u2/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Su 06:00-23:00", "shop" to "convenience"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.098812, longitude=20.009587),
                id = 26,
                osmMatch = ElementKey(ElementType.NODE, 11612210728),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 09:00-21:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID0D012-krakow-ul-os-bohaterow-wrzesnia-39d/"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:housenumber" to "39", "addr:postcode" to "31-621", "addr:street" to "Osiedle Bohaterów Września", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Su 06:00-23:00", "payment:apple_pay" to "yes", "payment:blik" to "yes", "payment:cash" to "yes", "payment:coins" to "yes", "payment:credit_cards" to "yes", "payment:google_pay" to "yes", "payment:notes" to "yes", "shop" to "convenience"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.084428, longitude=20.027786),
                id = 27,
                osmMatch = ElementKey(ElementType.NODE, 9059459532),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 09:00-21:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID0D380-krakow-os-os-jagiellonskie-19/"),
                tagsInOSM = mapOf("atm" to "yes", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "check_date" to "2024-03-23", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Su 06:00-23:00", "payment:blik" to "yes", "payment:cards" to "yes", "payment:cash" to "yes", "payment:contactless" to "yes", "shop" to "convenience"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.070713, longitude=20.037819),
                id = 28,
                osmMatch = ElementKey(ElementType.NODE, 11169336337),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 06:00-23:00; Su 08:00-20:00", "shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID0E220-krakow-ul-osiedle-centrum-e-23/"),
                tagsInOSM = mapOf("name" to "Żabka", "opening_hours" to "Mo-Sa 06:00-22:00; Su 08:00-20:00", "payment:credit_cards" to "yes", "payment:debit_cards" to "yes", "shop" to "convenience", "wheelchair" to "yes"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.08694930000001, longitude=20.0056977),
                id = 29,
                osmMatch = ElementKey(ElementType.WAY, 227349171),
                tagsInATP = mapOf("opening_hours" to "Mo-Sa 09:00-21:00; Su 10:00-18:00", "shop" to "electronics", "name" to "Media Expert", "brand" to "Media Expert", "brand:wikidata" to "Q11776794", "website" to "https://sklepy.mediaexpert.pl/krakow8_andersa"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:housenumber" to "8", "addr:postcode" to "31-930", "addr:street" to "Aleja generała Władysława Andersa", "brand" to "Media Expert", "brand:wikidata" to "Q11776794", "building" to "yes", "name" to "Media Expert", "opening_hours" to "Mo-Sa 09:00-21:00", "phone" to "+48 889 053 028", "shop" to "electronics", "source:geometry" to "geoportal.gov.pl:ortofoto", "was:name" to "Fiat - Salon & Service", "was:shop" to "car"),
                ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP,
            )
            ,
            AtpEntry(
                position = LatLon(latitude=50.09148, longitude=19.9885),
                id = 30,
                osmMatch = null,
                tagsInATP = mapOf("shop" to "frozen_food", "name" to "Wesoła Pani", "brand" to "Wesoła Pani", "brand:wikidata" to "Q123240454"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.08179, longitude=20.00081),
                id = 31,
                osmMatch = null,
                tagsInATP = mapOf("shop" to "frozen_food", "name" to "Wesoła Pani", "brand" to "Wesoła Pani", "brand:wikidata" to "Q123240454"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07324, longitude=20.02859),
                id = 32,
                osmMatch = ElementKey(ElementType.NODE, 9984802330),
                tagsInATP = mapOf("shop" to "frozen_food", "name" to "Wesoła Pani", "brand" to "Wesoła Pani", "brand:wikidata" to "Q123240454"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:housenumber" to "2", "addr:postcode" to "31-870", "addr:street" to "Mieczysława Medweckiego", "food" to "prepared_meals", "level" to "0", "opening_hours" to "Mo-Sa 09:00-21:00; Su 10:00-20:00", "shop" to "food"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.091498, longitude=19.987376),
                id = 33,
                osmMatch = ElementKey(ElementType.NODE, 10067213453),
                tagsInATP = mapOf("shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID0E588-krakow-ul-bohomolca-13-lok-lu-1/"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:housenumber" to "53A", "addr:postcode" to "31-416", "addr:street" to "Marchołta", "name" to "Żabka", "opening_hours" to "Mo-Su 06:00-23:00", "payment:cards" to "yes", "payment:cash" to "yes", "payment:credit_cards" to "yes", "payment:debit_cards" to "yes", "payment:mastercard" to "yes", "payment:telephone_cards" to "yes", "payment:visa" to "yes", "shop" to "supermarket"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.092896, longitude=20.014811),
                id = 34,
                osmMatch = ElementKey(ElementType.NODE, 9044149328),
                tagsInATP = mapOf("shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID02220-krakow-ul-mikolajczyka-os-kalinowe-12c/"),
                tagsInOSM = mapOf("brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "check_date:opening_hours" to "2024-11-18", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; PH,Su 10:00-21:00", "opening_hours:signed" to "no", "shop" to "convenience", "wheelchair" to "yes"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.080458, longitude=20.003712),
                id = 35,
                osmMatch = ElementKey(ElementType.NODE, 9168905169),
                tagsInATP = mapOf("shop" to "convenience", "name" to "Żabka", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "website" to "https://www.zabka.pl/znajdz-sklep/ID06239-krakow-os-avia-3-lok-u1/"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:housenumber" to "3", "addr:place" to "Osiedle Avia", "addr:postcode" to "31-877", "addr:unit" to "U1", "brand" to "Żabka", "brand:wikidata" to "Q2589061", "brand:wikipedia" to "pl:Żabka (sieć sklepów)", "name" to "Żabka", "name:pl" to "Żabka", "name:uk" to "Жабка", "opening_hours" to "Mo-Sa 06:00-23:00; Su,PH 11:00-19:00", "shop" to "convenience"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.088771, longitude=19.986762),
                id = 36,
                osmMatch = ElementKey(ElementType.NODE, 9021402707),
                tagsInATP = mapOf("shop" to "clothes", "name" to "H&M", "brand" to "H&M", "brand:wikidata" to "Q188326"),
                tagsInOSM = mapOf("shop" to "clothes"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0875, longitude=19.98727),
                id = 37,
                osmMatch = ElementKey(ElementType.WAY, 1074322018),
                tagsInATP = mapOf("amenity" to "cafe", "name" to "Starbucks", "brand" to "Starbucks", "brand:wikidata" to "Q37158", "website" to "https://www.starbucks.co.uk/store-locator/51856-261544/krakow,-serenada"),
                tagsInOSM = mapOf("amenity" to "fast_food", "building" to "kiosk", "building:levels" to "1", "outdoor_seating" to "yes"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0884449, longitude=19.9942296),
                id = 38,
                osmMatch = ElementKey(ElementType.NODE, 5871989987),
                tagsInATP = mapOf("shop" to "convenience", "name" to "Odido", "brand" to "Odido", "brand:wikidata" to "Q106947294", "website" to "https://www.sklepy-odido.pl/znajdz-sklep/osiedle-oswiecenia-21"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:country" to "PL", "addr:housenumber" to "76", "addr:place" to "Osiedle Tysiąclecia", "shop" to "convenience"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0833298, longitude=20.0168437),
                id = 39,
                osmMatch = null,
                tagsInATP = mapOf("shop" to "convenience", "name" to "Odido", "brand" to "Odido", "brand:wikidata" to "Q106947294", "website" to "https://www.sklepy-odido.pl/znajdz-sklep/kosciuszkowskie-6a"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0819207, longitude=20.011011),
                id = 40,
                osmMatch = null,
                tagsInATP = mapOf("amenity" to "post_office", "name" to "Epaka.pl", "brand" to "Epaka.pl", "brand:wikidata" to "Q123028724", "website" to "https://krakow-dywizjonu303.epaka.pl"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.073014, longitude=20.036947),
                id = 41,
                osmMatch = ElementKey(ElementType.NODE, 4271289402),
                tagsInATP = mapOf("amenity" to "pharmacy", "name" to "Ziko Apteka", "brand" to "Ziko Apteka", "brand:wikidata" to "Q63432892", "website" to "https://zikoapteka.pl/apteki/krakow/centrum-c1/"),
                tagsInOSM = mapOf("amenity" to "pharmacy", "brand" to "Ziko Apteka", "brand:wikidata" to "Q63432892", "check_date:opening_hours" to "2025-04-25", "dispensing" to "yes", "healthcare" to "pharmacy", "name" to "Ziko Apteka", "opening_hours" to "Mo-Fr 08:00-21:00, Sa 08:00-16:00", "payment:cash" to "yes", "payment:coins" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "phone" to "+48 12 687 57 47", "ref:csioz" to "1188707", "short_name" to "Ziko", "trash_accepted:medicines" to "yes", "website" to "https://www.ziko.pl/krakow-apteki-drogerie-ziko/centrum-c1/", "wheelchair" to "yes"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.084492, longitude=20.028099),
                id = 42,
                osmMatch = ElementKey(ElementType.NODE, 6375904177),
                tagsInATP = mapOf("amenity" to "pharmacy", "name" to "Ziko Apteka", "brand" to "Ziko Apteka", "brand:wikidata" to "Q63432892", "website" to "https://zikoapteka.pl/apteki/krakow/jagiellonskie-19/"),
                tagsInOSM = mapOf("amenity" to "pharmacy", "brand" to "Ziko Apteka", "brand:wikidata" to "Q63432892", "dispensing" to "yes", "healthcare" to "pharmacy", "name" to "Ziko Apteka", "opening_hours" to "Mo-Fr 08:00-20:00; Sa 08:00-16:00", "payment:cash" to "yes", "payment:coins" to "yes", "payment:mastercard" to "yes", "payment:visa" to "yes", "phone" to "+48 12 687 57 47", "ref:csioz" to "1034564", "short_name" to "Ziko", "website" to "https://www.ziko.pl/krakow-apteki-drogerie-ziko/jagiellonskie-19/"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.101578, longitude=20.013521),
                id = 43,
                osmMatch = ElementKey(ElementType.NODE, 1819649746),
                tagsInATP = mapOf("shop" to "supermarket", "name" to "Lewiatan", "brand" to "Lewiatan", "brand:wikidata" to "Q11755396", "website" to "https://www.lewiatan.pl/znajdz-sklep/lewiatan-krakow-os-piastow-60"),
                tagsInOSM = mapOf("shop" to "convenience"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.08688540000001, longitude=19.9859261),
                id = 44,
                osmMatch = null,
                tagsInATP = mapOf("shop" to "cosmetics", "name" to "Rituals", "brand" to "Rituals", "brand:wikidata" to "Q62874140", "website" to "https://www.rituals.com/pl-pl/store-detail?store=Krak%C3%B3w-Centrum-Serenada"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07237, longitude=19.98085),
                id = 45,
                osmMatch = ElementKey(ElementType.NODE, 11191147252),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Appkomat InPost", "website" to "https://inpost.pl/paczkomat-krakow-kra05app-sliczna-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "parcel_mail_in" to "yes", "parcel_pickup" to "yes"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.08659, longitude=19.97448),
                id = 46,
                osmMatch = ElementKey(ElementType.NODE, 12806473844),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra07ap-lublanska-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.08449, longitude=19.98387),
                id = 47,
                osmMatch = ElementKey(ElementType.NODE, 12492320040),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra142m-cieslewskiego-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "indoor" to "yes", "level" to "0", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA02HO"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07263, longitude=19.96887),
                id = 48,
                osmMatch = ElementKey(ElementType.NODE, 9424164484),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra151m-lotnicza-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA240M"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.09754, longitude=19.97367),
                id = 49,
                osmMatch = ElementKey(ElementType.NODE, 3891470345),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra16a-strzelcow-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "description" to "Paczkomat przy markecie Lewiatan", "image" to "https://geowidget.easypack24.net/uploads/pl/images/KRA16A.jpg", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "payment:cash" to "no", "payment:credit_cards" to "yes", "payment:cryptocurrencies" to "no", "payment:debit_cards" to "yes", "payment:electronic_purses" to "no", "payment:wire_transfer" to "yes", "ref" to "KRA16A"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07284, longitude=19.98028),
                id = 50,
                osmMatch = ElementKey(ElementType.NODE, 7503479528),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra16n-meissnera-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "check_date" to "2024-10-12", "description" to "Paczkomat przy Delikatesach Podwawelskich", "image" to "https://geowidget.easypack24.net/uploads/pl/images/KRA16N.jpg", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "payment:cash" to "no", "payment:credit_cards" to "yes", "payment:cryptocurrencies" to "no", "payment:debit_cards" to "yes", "payment:electronic_purses" to "no", "ref" to "KRA16N"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.08959, longitude=19.99084),
                id = 51,
                osmMatch = ElementKey(ElementType.NODE, 8978596238),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra301m-os-oswiecenia-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA39A"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07695, longitude=19.98058),
                id = 52,
                osmMatch = ElementKey(ElementType.NODE, 12630124675),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra326m-ugorek-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "description" to "obok Pawilonu Handlowego", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA409M", "wheelchair" to "yes"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.08329, longitude=19.97582),
                id = 53,
                osmMatch = ElementKey(ElementType.NODE, 3891498858),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra330m-dzielskiego-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "description" to "Paczkomat przy stacji BP", "image" to "https://geowidget.easypack24.net/uploads/pl/images/KRA07MP.jpg", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "payment:cash" to "no", "payment:credit_cards" to "yes", "payment:cryptocurrencies" to "no", "payment:debit_cards" to "yes", "payment:electronic_purses" to "no", "ref" to "KRA07MP"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0828, longitude=19.9722),
                id = 54,
                osmMatch = ElementKey(ElementType.NODE, 3891498858),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra332m-mlynska-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "description" to "Paczkomat przy stacji BP", "image" to "https://geowidget.easypack24.net/uploads/pl/images/KRA07MP.jpg", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "payment:cash" to "no", "payment:credit_cards" to "yes", "payment:cryptocurrencies" to "no", "payment:debit_cards" to "yes", "payment:electronic_purses" to "no", "ref" to "KRA07MP"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.09299, longitude=19.98919),
                id = 55,
                osmMatch = ElementKey(ElementType.NODE, 9355699725),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra340m-oswiecenia-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA244M"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.09217, longitude=19.99215),
                id = 56,
                osmMatch = ElementKey(ElementType.WAY, 1206232704),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra44m-bohdana-arcta-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "operator" to "InPost", "operator:wikidata" to "Q3182097", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA227M"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.08593, longitude=20.01884),
                id = 57,
                osmMatch = ElementKey(ElementType.NODE, 6580361421),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra01a-wladyslawa-broniewskiego-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "check_date" to "2023-06-22", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA01A"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07177, longitude=20.02873),
                id = 58,
                osmMatch = ElementKey(ElementType.NODE, 5300249939),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra02ap-al-jana-pawla-ii-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "check_date" to "2024-10-10", "description" to "Stacja paliw Circle K - Paczkomat Hi-Shine", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA02AP"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07993, longitude=20.02129),
                id = 59,
                osmMatch = ElementKey(ElementType.NODE, 11931594478),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra04ap-os-niepodleglosci-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("addr:street" to "Gustawa Pokrzywki", "amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "collection_times" to "24/7", "indoor" to "no", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "payment:app" to "yes", "ref" to "KRA392M"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.08721, longitude=20.02516),
                id = 60,
                osmMatch = ElementKey(ElementType.NODE, 12320865634),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra04bm-os-jagiellonskie-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.08219, longitude=20.00049),
                id = 61,
                osmMatch = ElementKey(ElementType.NODE, 11255599778),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra10n-orlinskiego-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "payment:inpost_mobile" to "yes", "payment:paybylink" to "yes", "ref" to "KRA10N"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.08185, longitude=20.00206),
                id = 62,
                osmMatch = ElementKey(ElementType.NODE, 11255599779),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra110m-orlinskiego-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "payment:inpost_mobile" to "yes", "payment:paybylink" to "yes", "ref" to "KRA110M"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.09355, longitude=20.02259),
                id = 63,
                osmMatch = ElementKey(ElementType.NODE, 6783711185),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra11a-okulickiego-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA11A", "website" to "https://inpost.pl/paczkomat-krakow-kra11a-okulickiego-paczkomaty-malopolskie"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.09448, longitude=20.02332),
                id = 64,
                osmMatch = ElementKey(ElementType.NODE, 9348418009),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra144m-okulickiego-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA144M", "website" to "https://inpost.pl/paczkomat-krakow-kra144m-okulickiego-paczkomaty-malopolskie"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.097, longitude=20.02011),
                id = 65,
                osmMatch = ElementKey(ElementType.NODE, 11725186087),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra150m-os-piastow-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "collection_times" to "24/7", "indoor" to "no", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "payment:app" to "yes", "ref" to "KRA396M", "wheelchair" to "yes"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.09368, longitude=20.02384),
                id = 66,
                osmMatch = ElementKey(ElementType.NODE, 6783711085),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra15ap-okulickiego-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "payment:credit_cards" to "yes", "ref" to "KRA15AP"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07732, longitude=20.04125),
                id = 67,
                osmMatch = ElementKey(ElementType.NODE, 11406979602),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Appkomat InPost", "website" to "https://inpost.pl/paczkomat-krakow-kra15app-os-sloneczne-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("addr:city" to "Kraków", "addr:housenumber" to "3A", "addr:place" to "Osiedle Słoneczne", "addr:postcode" to "31-956", "amenity" to "parcel_locker", "app_operated" to "only", "brand" to "Appkomat InPost", "description" to "Przy budynku Administracji ADREM", "image" to "https://geowidget.easypack24.net/uploads/pl/images/KRA15APP.jpg", "not:brand:wikidata" to "Q110970254", "opening_hours" to "24/7", "ref" to "KRA15APP", "website" to "https://inpost.pl/paczkomat-krakow-kra15app-os-sloneczne-paczkomaty-malopolskie"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.08153, longitude=20.04798),
                id = 68,
                osmMatch = ElementKey(ElementType.NODE, 11942568657),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra161m-bulwarowa-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "description" to "Przy Centrum Kształcenia Zawodowego i Ustawicznego (z boku budynku)", "image" to "https://geowidget.easypack24.net/uploads/pl/images/KRA327M.jpg", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA327M", "website" to "https://inpost.pl/paczkomat-krakow-kra327m-os-szkolne-paczkomaty-malopolskie"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07684, longitude=20.04111),
                id = 69,
                osmMatch = ElementKey(ElementType.NODE, 9115281574),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra176m-os-sloneczne-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA176M"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07754, longitude=20.02328),
                id = 70,
                osmMatch = ElementKey(ElementType.NODE, 11612845543),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra189m-bienczycka-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "operator" to "InPost", "operator:wikidata" to "Q3182097", "parcel_mail_in" to "yes", "parcel_pickup" to "yes"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07023, longitude=20.03964),
                id = 71,
                osmMatch = ElementKey(ElementType.NODE, 7071798905),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra18m-centrum-e-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "check_date" to "2025-05-19", "covered" to "yes", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "ref" to "KRA18M"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0704, longitude=20.04097),
                id = 72,
                osmMatch = ElementKey(ElementType.NODE, 8809812080),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra204m-os-centrum-e-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "check_date" to "2024-09-14", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA204M"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.08625, longitude=20.03139),
                id = 73,
                osmMatch = ElementKey(ElementType.NODE, 8742051523),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Appkomat InPost", "website" to "https://inpost.pl/paczkomat-krakow-kra22bapp-cienista-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA11M"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.08747, longitude=20.02697),
                id = 74,
                osmMatch = ElementKey(ElementType.NODE, 12320865634),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Appkomat InPost", "website" to "https://inpost.pl/paczkomat-krakow-kra23bapp-os-jagiellonskie-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.09149, longitude=20.00207),
                id = 75,
                osmMatch = ElementKey(ElementType.NODE, 5439444266),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra273m-tysiaclecia-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "check_date" to "2024-12-24", "image" to "https://geowidget.easypack24.net/uploads/pl/images/KRA14A.jpg", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "payment:inpost_mobile" to "yes", "payment:paybylink" to "yes", "ref" to "KRA14A", "website" to "https://inpost.pl/paczkomat-krakow-kra14a-os-kombatantow-paczkomaty-malopolskie"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.09362, longitude=20.00148),
                id = 76,
                osmMatch = ElementKey(ElementType.NODE, 12514512981),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra274m-osiedle-tysiaclecia-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.09528, longitude=20.04483),
                id = 77,
                osmMatch = ElementKey(ElementType.NODE, 5661627539),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra334m-wladyslawa-jagielly-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA09N"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07218, longitude=20.01938),
                id = 78,
                osmMatch = ElementKey(ElementType.NODE, 6228111085),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra38a-al-jana-pawla-ii-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "check_date" to "2025-05-02", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA38A"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.08205, longitude=20.00109),
                id = 79,
                osmMatch = ElementKey(ElementType.NODE, 11799906762),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra398m-orlinskiego-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "description" to "Obok parkingu, środkowy Paczkomat", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "payment:cards" to "yes", "payment:paybylink" to "yes", "ref" to "KRA398M", "website" to "https://inpost.pl/paczkomat-krakow-kra398m-orlinskiego-paczkomaty-malopolskie", "wheelchair" to "yes"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07098, longitude=20.02846),
                id = 80,
                osmMatch = ElementKey(ElementType.NODE, 7059984883),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra40n-bp-tomickiego-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "check_date" to "2025-01-08", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA40N"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07078, longitude=20.03753),
                id = 81,
                osmMatch = ElementKey(ElementType.NODE, 7071798905),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra416m-os-centrum-e-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "check_date" to "2025-05-19", "covered" to "yes", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "ref" to "KRA18M"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07907, longitude=20.00275),
                id = 82,
                osmMatch = ElementKey(ElementType.NODE, 11255599779),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra420m-avia-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "payment:inpost_mobile" to "yes", "payment:paybylink" to "yes", "ref" to "KRA110M"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0844, longitude=20.04021),
                id = 83,
                osmMatch = ElementKey(ElementType.NODE, 11450670732),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra43a-kocmyrzowska-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "check_date" to "2025-02-15", "operator" to "DHL"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0819, longitude=20.02209),
                id = 84,
                osmMatch = ElementKey(ElementType.NODE, 12078371708),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra48n-niepodleglosci-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "operator" to "InPost", "operator:wikidata" to "Q3182097", "parcel_mail_in" to "yes", "parcel_pickup" to "yes"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.07401, longitude=20.02686),
                id = 85,
                osmMatch = ElementKey(ElementType.NODE, 5300249939),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra56m-os-kolorowe-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "check_date" to "2024-10-10", "description" to "Stacja paliw Circle K - Paczkomat Hi-Shine", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "operator:wikipedia" to "pl:InPost", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "ref" to "KRA02AP"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.09728, longitude=20.0201),
                id = 86,
                osmMatch = ElementKey(ElementType.NODE, 11725186087),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra58m-os-piastow-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "collection_times" to "24/7", "indoor" to "no", "opening_hours" to "24/7", "operator" to "InPost", "operator:wikidata" to "Q3182097", "parcel_mail_in" to "yes", "parcel_pickup" to "yes", "payment:app" to "yes", "ref" to "KRA396M", "wheelchair" to "yes"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.09026, longitude=20.0262),
                id = 87,
                osmMatch = ElementKey(ElementType.NODE, 12320857335),
                tagsInATP = mapOf("amenity" to "parcel_locker", "brand" to "Paczkomat InPost", "brand:wikidata" to "Q110970254", "website" to "https://inpost.pl/paczkomat-krakow-kra78m-zlotej-jesieni-paczkomaty-malopolskie"),
                tagsInOSM = mapOf("amenity" to "parcel_locker"),
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0804135, longitude=19.9926123),
                id = 88,
                osmMatch = null,
                tagsInATP = mapOf("amenity" to "bank", "name" to "Alior Bank", "brand" to "Alior Bank", "brand:wikidata" to "Q9148395"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0804135, longitude=19.9926123),
                id = 89,
                osmMatch = null,
                tagsInATP = mapOf("amenity" to "bank", "name" to "Alior Bank", "brand" to "Alior Bank", "brand:wikidata" to "Q9148395"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.0804135, longitude=19.9926123),
                id = 90,
                osmMatch = null,
                tagsInATP = mapOf("amenity" to "bank", "name" to "Alior Bank", "brand" to "Alior Bank", "brand:wikidata" to "Q9148395"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            ), AtpEntry(
                position = LatLon(latitude=50.095042, longitude=20.011993),
                id = 91,
                osmMatch = null,
                tagsInATP = mapOf("amenity" to "bank", "name" to "Alior Bank", "brand" to "Alior Bank", "brand:wikidata" to "Q9148395"),
                tagsInOSM = null,
                ReportType.MISSING_POI_IN_OPENSTREETMAP,
            )
        )
    }

    companion object {
        private const val TAG = "AtpDownload"
    }
}
