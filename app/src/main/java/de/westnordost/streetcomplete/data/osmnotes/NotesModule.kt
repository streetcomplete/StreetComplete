package de.westnordost.streetcomplete.data.osmnotes

import android.content.Context
import de.westnordost.streetcomplete.ApplicationConstants
import kotlinx.io.files.Path
import org.koin.core.qualifier.named
import org.koin.dsl.module

val notesModule = module {
    factory(named("AvatarsCacheDirectory")) { Path(get<Context>().cacheDir.path, ApplicationConstants.AVATARS_CACHE_DIRECTORY) }
    factory { AvatarsDownloader(get(), get(), get(), get(named("AvatarsCacheDirectory"))) }
    factory { AvatarsInNotesUpdater(get()) }
    factory { NoteDao(get()) }
    factory { NotesDownloader(get(), get()) }
    factory { PhotoServiceApiClient(get(), get(), ApplicationConstants.SC_PHOTO_SERVICE_URL) }

    single {
        NoteController(get()).apply {
            // on notes have been updated, avatar images should be downloaded (cached) referenced in note discussions
            addListener(get<AvatarsInNotesUpdater>())
        }
    }
}
