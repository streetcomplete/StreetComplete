package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.OSM_API_URL
import de.westnordost.streetcomplete.data.osmnotes.AvatarsInNotesUpdater
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.NotesApiClient
import de.westnordost.streetcomplete.data.osmnotes.NotesApiParser
import org.koin.dsl.module

val atpModule = module {
    factory { AtpDao(get()) }
    factory { AtpDownloader(get(), get()) }
    // TODO: connect to actual global API
    // current holders:
    // https://gist.github.com/matkoniecz/163c0bca9d03efc33d744f6091c91904
    // https://codeberg.org/matkoniecz/experimental_read_only_api_for_atp_osm_work
    // https://matkoniecz.codeberg.page/improving_openstreetmap_using_alltheplaces_dataset/read_only_api_prototype/lat_50/lon_20_gathered.geojson
    val OSM_ATP_COMPARISON_API_BASE_URL = "https://matkoniecz.codeberg.page/improving_openstreetmap_using_alltheplaces_dataset/read_only_api_prototype/"
    factory { AtpApiClient(get(), OSM_ATP_COMPARISON_API_BASE_URL, get()) }
    factory { AtpApiParser() }

    single {
        AtpController(get()).apply {
        }
    }
}
