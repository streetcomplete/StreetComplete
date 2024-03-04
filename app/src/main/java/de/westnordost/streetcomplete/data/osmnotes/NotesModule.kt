package de.westnordost.streetcomplete.data.osmnotes

import android.content.Context
import de.westnordost.streetcomplete.ApplicationConstants
import okio.Path.Companion.toOkioPath
import org.koin.dsl.binds
import org.koin.dsl.module

val notesModule = module {
    factory {
        val avatarCacheDir = get<Context>().cacheDir.toOkioPath().resolve(ApplicationConstants.AVATARS_CACHE_DIRECTORY)
        AvatarsDownloader(get(), get(), get(), avatarCacheDir)
    } binds arrayOf(AvatarStore::class, AvatarsDownloader::class)
    factory { AvatarsInNotesUpdater(get()) }
    factory { NoteDao(get()) }
    factory { NotesDownloader(get(), get()) }
    factory { StreetCompleteImageUploader(get(), get(), ApplicationConstants.SC_PHOTO_SERVICE_URL) }

    single {
        NoteController(get()).apply {
            // on notes have been updated, avatar images should be downloaded (cached) referenced in note discussions
            addListener(get<AvatarsInNotesUpdater>())
        }
    }
}
