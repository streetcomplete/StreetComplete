package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.ApplicationConstants
import org.koin.core.qualifier.named
import org.koin.dsl.module

val notesModule = module {
    factory { AvatarsDownloader(get(), get(), get(), get(named("AvatarsCacheDirectory"))) }
    factory { AvatarsInNotesUpdater(get()) }
    factory { NoteDao(get()) }
    factory { NotesDownloader(get(), get()) }
    factory<PhotoServiceApiClient> { PhotoServiceApiClientImpl(get(), get(), ApplicationConstants.SC_PHOTO_SERVICE_URL) }

    single<NoteSource> { get<NoteController>() }
    single<NoteController> {
        NoteControllerImpl(get()).apply {
            // on notes have been updated, avatar images should be downloaded (cached) referenced in note discussions
            addListener(get<AvatarsInNotesUpdater>())
        }
    }
}
