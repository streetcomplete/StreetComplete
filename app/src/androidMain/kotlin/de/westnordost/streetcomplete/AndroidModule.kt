package de.westnordost.streetcomplete

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
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
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val androidModule = module {
    // Workmanager-based on Android

    single<UploadController> { UploadControllerAndroid(androidContext()) }
    worker { UploadWorker(get(), androidContext(), get()) }

    single<DownloadController> { DownloadControllerAndroid(androidContext()) }
    worker { DownloadWorker(get(), androidContext(), get()) }

    factory<ChangesetAutoCloser> { ChangesetAutoCloserAndroid(androidContext()) }
    worker { ChangesetAutoCloserWorker(get(), androidContext(), get()) }

    factory<MapTilesDownloader> { MapTilesDownloaderAndroid(androidContext()) }

    factory<InternetConnectionState> { InternetConnectionState(androidContext()) }

    // Settings

    single<ObservableSettings> { SharedPreferencesSettings.Factory(androidContext()).create() }
}
