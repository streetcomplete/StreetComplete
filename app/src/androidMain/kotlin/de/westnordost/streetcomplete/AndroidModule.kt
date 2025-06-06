package de.westnordost.streetcomplete

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import de.westnordost.streetcomplete.data.AndroidDatabase
import de.westnordost.streetcomplete.data.CleanerWorker
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.StreetCompleteSQLiteOpenHelper
import de.westnordost.streetcomplete.data.connection.InternetConnectionState
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.download.DownloadControllerAndroid
import de.westnordost.streetcomplete.data.download.DownloadWorker
import de.westnordost.streetcomplete.data.maptiles.MapTilesDownloader
import de.westnordost.streetcomplete.data.maptiles.MapTilesDownloaderAndroid
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetAutoCloser
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetAutoCloserAndroid
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetAutoCloserWorker
import de.westnordost.streetcomplete.data.upload.UploadController
import de.westnordost.streetcomplete.data.upload.UploadControllerAndroid
import de.westnordost.streetcomplete.data.upload.UploadWorker
import kotlinx.io.files.Path
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.named
import org.koin.dsl.module

val androidModule = module {
    // Database on Android

    single<Database> {
        val sqLite = StreetCompleteSQLiteOpenHelper(get(), ApplicationConstants.DATABASE_NAME)
        AndroidDatabase(sqLite.writableDatabase)
    }

    // Workmanager-based on Android

    single<UploadController> { UploadControllerAndroid(androidContext()) }
    worker { UploadWorker(get(), androidContext(), get()) }

    single<DownloadController> { DownloadControllerAndroid(androidContext()) }
    worker { DownloadWorker(get(), androidContext(), get()) }

    factory<ChangesetAutoCloser> { ChangesetAutoCloserAndroid(androidContext()) }
    worker { ChangesetAutoCloserWorker(get(), androidContext(), get()) }

    worker { CleanerWorker(get(), get(), get()) }

    factory<MapTilesDownloader> { MapTilesDownloaderAndroid(androidContext()) }

    factory<InternetConnectionState> { InternetConnectionState(androidContext()) }

    // Cache dir

    factory(named("AvatarsCacheDirectory")) {
        Path(
            androidContext().cacheDir.path,
            ApplicationConstants.AVATARS_CACHE_DIRECTORY
        )
    }

    // Settings

    single<ObservableSettings> { SharedPreferencesSettings.Factory(androidContext()).create() }
}
