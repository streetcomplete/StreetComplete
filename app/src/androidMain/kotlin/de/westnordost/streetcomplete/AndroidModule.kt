package de.westnordost.streetcomplete

import android.content.res.AssetManager
import android.content.res.Resources
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.create
import de.westnordost.streetcomplete.data.AndroidDatabase
import de.westnordost.streetcomplete.data.CleanerWorker
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.StreetCompleteSQLiteOpenHelper
import de.westnordost.streetcomplete.data.connection.AndroidActiveNetworkConnection
import de.westnordost.streetcomplete.data.connection.ActiveNetworkConnection
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
import de.westnordost.streetcomplete.screens.about.AndroidAppStoreInfo
import de.westnordost.streetcomplete.screens.about.AppStoreInfo
import de.westnordost.streetcomplete.screens.main.AndroidEmailAppLauncher
import de.westnordost.streetcomplete.screens.main.AndroidMapAppLauncher
import de.westnordost.streetcomplete.screens.main.EmailAppLauncher
import de.westnordost.streetcomplete.screens.main.MapAppLauncher
import de.westnordost.streetcomplete.ui.util.measure.AndroidArMeasureAppLauncher
import de.westnordost.streetcomplete.ui.util.measure.AndroidArSupportChecker
import de.westnordost.streetcomplete.ui.util.measure.ArMeasureAppLauncher
import de.westnordost.streetcomplete.ui.util.measure.ArSupportChecker
import de.westnordost.streetcomplete.ui.util.photo.AndroidHasCameraChecker
import de.westnordost.streetcomplete.ui.util.photo.HasCameraChecker
import de.westnordost.streetcomplete.util.error_reporting.CrashReportHolder
import de.westnordost.streetcomplete.util.error_reporting.CrashReportsUncaughtExceptionHandler
import de.westnordost.streetcomplete.util.location.LocationAvailabilityReceiver
import de.westnordost.streetcomplete.util.logs.DatabaseLogger
import de.westnordost.streetcomplete.util.sound.AndroidSoundEffectPlayer
import de.westnordost.streetcomplete.util.sound.SoundEffectPlayer
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.scope.dsl.activityScope
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val COMPOSE_FILES_DIR = "composeResources/de.westnordost.streetcomplete.resources/files"

val androidModule = module {

    // metadata

    single<de.westnordost.countryboundaries.CountryBoundaries> {
        val source = androidContext().assets.open(COMPOSE_FILES_DIR + "/boundaries.ser").asSource().buffered()
        de.westnordost.countryboundaries.CountryBoundaries.deserializeFrom(source)
    }

    single<FeatureDictionary> {
        FeatureDictionary.create(
            assetManager = androidContext().assets,
            presetsBasePath = COMPOSE_FILES_DIR + "/osmfeatures/default",
            brandPresetsBasePath = COMPOSE_FILES_DIR + "/osmfeatures/brands"
        )
    }

    // error reporting

    single { CrashReportsUncaughtExceptionHandler(androidContext(), get(), "crashreport.txt") }
    single<CrashReportHolder> { get<CrashReportsUncaughtExceptionHandler>() }

    // database

    single<Database> {
        val sqLite = StreetCompleteSQLiteOpenHelper(get(), ApplicationConstants.DATABASE_NAME)
        AndroidDatabase(sqLite.writableDatabase)
    }

    // avatars cache dir

    factory(named("AvatarsCacheDirectory")) {
        Path(androidContext().cacheDir.path, ApplicationConstants.AVATARS_CACHE_DIRECTORY)
    }

    // app store info

    single<AppStoreInfo> { AndroidAppStoreInfo(get()) }

    // take photos

    factory<HasCameraChecker>() { AndroidHasCameraChecker(get()) }

    // AR

    factory<ArSupportChecker> { AndroidArSupportChecker(get()) }
    activityScope {
        scoped<ArMeasureAppLauncher> { AndroidArMeasureAppLauncher({ get() }) }
    }

    // launch apps

    factory<MapAppLauncher> { AndroidMapAppLauncher(get()) }
    factory<EmailAppLauncher> { AndroidEmailAppLauncher(get()) }

    // settings

    single<ObservableSettings> { SharedPreferencesSettings.Factory(androidContext()).create() }

    // sound

    single<SoundEffectPlayer> { AndroidSoundEffectPlayer(androidContext(), COMPOSE_FILES_DIR) }

    // connection availability

    factory<ActiveNetworkConnection> { AndroidActiveNetworkConnection(androidContext()) }

    // location availability

    single { LocationAvailabilityReceiver(get()) }

    // background jobs

    single<UploadController> { UploadControllerAndroid(androidContext()) }
    worker { UploadWorker(get(), androidContext(), get()) }

    single<DownloadController> { DownloadControllerAndroid(androidContext()) }
    worker { DownloadWorker(get(), androidContext(), get()) }

    factory<ChangesetAutoCloser> { ChangesetAutoCloserAndroid(androidContext()) }
    worker { ChangesetAutoCloserWorker(get(), androidContext(), get()) }

    worker { CleanerWorker(get(), get(), get()) }

    factory<MapTilesDownloader> { MapTilesDownloaderAndroid(androidContext()) }
}
