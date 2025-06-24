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
    // TODO: connect to actual bidirectional API
    val OSM_ATP_COMPARISON_API_BASE_URL = "https://matkoniecz.codeberg.page/improving_openstreetmap_using_alltheplaces_dataset/read_only_api_prototype/"
    factory { AtpApiClient(get(), OSM_ATP_COMPARISON_API_BASE_URL, get()) }
    factory { AtpApiParser() }

    single {
        AtpController(get()).apply {
        }
    }
}
