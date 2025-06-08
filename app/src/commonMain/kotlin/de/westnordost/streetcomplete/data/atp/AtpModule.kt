package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.OSM_API_URL
import de.westnordost.streetcomplete.data.osmnotes.AvatarsInNotesUpdater
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.NotesApiClient
import org.koin.dsl.module

val atpModule = module {
    factory { AtpDao(get()) }
    factory { AtpDownloader(get(), get()) }
    factory { AtpApiClient(get(), OSM_API_URL) } // TODO fix base URL, where it is even defined?

    single {
        AtpController(get()).apply {
        }
    }
}
