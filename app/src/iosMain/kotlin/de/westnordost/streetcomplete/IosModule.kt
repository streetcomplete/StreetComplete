package de.westnordost.streetcomplete

import androidx.sqlite.driver.NativeSQLiteDriver
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.DatabaseImpl
import de.westnordost.streetcomplete.data.IosPeriodicCleaner
import de.westnordost.streetcomplete.data.PeriodicCleaner
import de.westnordost.streetcomplete.data.StreetCompleteDatabaseConfigurator
import de.westnordost.streetcomplete.data.connection.ActiveNetworkConnection
import de.westnordost.streetcomplete.data.connection.IosActiveNetworkConnection
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.download.Downloader
import de.westnordost.streetcomplete.data.initialize
import de.westnordost.streetcomplete.data.maptiles.MapLibreMapTilesDownloader
import de.westnordost.streetcomplete.data.maptiles.MapTilesDownloader
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetAutoCloser
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.sync.CoroutineChangesetAutoCloser
import de.westnordost.streetcomplete.data.sync.CoroutineDownloadController
import de.westnordost.streetcomplete.data.sync.CoroutineUploadController
import de.westnordost.streetcomplete.data.sync.IosBackgroundSyncController
import de.westnordost.streetcomplete.data.upload.UploadController
import de.westnordost.streetcomplete.data.upload.Uploader
import de.westnordost.streetcomplete.screens.about.AppStoreInfo
import de.westnordost.streetcomplete.screens.about.IosAppStoreInfo
import de.westnordost.streetcomplete.ui.util.measure.ArSupportChecker
import de.westnordost.streetcomplete.ui.util.measure.IosArSupportChecker
import de.westnordost.streetcomplete.util.error_reporting.CrashReportHolder
import de.westnordost.streetcomplete.util.error_reporting.EmptyCrashReportHolder
import de.westnordost.streetcomplete.util.sound.IosSoundEffectPlayer
import de.westnordost.streetcomplete.util.sound.SoundEffectPlayer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.maplibre.compose.location.IosLocationProvider
import org.maplibre.compose.location.IosSystemSettingsLauncher
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.SystemSettingsLauncher
import org.maplibre.compose.map.MapRuntime
import org.maplibre.compose.map.MapRuntimeOptions
import org.maplibre.compose.map.createMapRuntime
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSBundle
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIScreen

private val COMPOSE_FILES_DIR = NSBundle.mainBundle.resourcePath +
    "/compose-resources/composeResources/de.westnordost.streetcomplete.resources/files"

@OptIn(ExperimentalForeignApi::class)
val iosModule = module {

    // metadata

    single<de.westnordost.countryboundaries.CountryBoundaries> {
        val file = Path(COMPOSE_FILES_DIR + "/boundaries.ser")
        val source = SystemFileSystem.source(file).buffered()
        de.westnordost.countryboundaries.CountryBoundaries.deserializeFrom(source)
    }

    single<FeatureDictionary> {
        FeatureDictionary.create(
            fileSystem = SystemFileSystem,
            presetsBasePath = COMPOSE_FILES_DIR + "/osmfeatures/default",
            brandPresetsBasePath = COMPOSE_FILES_DIR + "/osmfeatures/brands"
        )
    }

    // error reporting

    single<CrashReportHolder> { EmptyCrashReportHolder }

    // database

    single<Database> {
        val databaseConnection = NativeSQLiteDriver().open(
            applicationSupportPath(ApplicationConstants.DATABASE_NAME).toString()
        )
        DatabaseImpl(databaseConnection).apply { initialize(StreetCompleteDatabaseConfigurator) }
    } onClose { it?.close() }

    // avatars cache dir

    factory(named("AvatarsCacheDirectory")) {
        val cacheUrl = NSFileManager.defaultManager.URLForDirectory(
            directory = NSCachesDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )!!
        val avatarsCacheUrl = cacheUrl.URLByAppendingPathComponent(ApplicationConstants.AVATARS_CACHE_DIRECTORY)!!
        Path(avatarsCacheUrl.path!!)
    }

    // app store info

    single<AppStoreInfo> { IosAppStoreInfo }

    // AR

    factory<ArSupportChecker> { IosArSupportChecker() }

    // location

    single<LocationProvider> { IosLocationProvider() }
    factory<SystemSettingsLauncher> { IosSystemSettingsLauncher() }

    // settings

    single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }

    // sound

    single<SoundEffectPlayer> { IosSoundEffectPlayer(COMPOSE_FILES_DIR) }

    // connection

    single<ActiveNetworkConnection> { IosActiveNetworkConnection() }

    // map runtime and offline base-map storage

    single<MapRuntime> { createMapRuntime(MapRuntimeOptions()) }
    factory<MapTilesDownloader> {
        MapLibreMapTilesDownloader(
            runtime = get(),
            pixelRatio = UIScreen.mainScreen.scale.toFloat(),
        )
    }

    // background jobs

    single {
        IosBackgroundSyncController(
            get<CoroutineScope>(named("ApplicationScope")),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }

    single<UploadController> {
        CoroutineUploadController(get<CoroutineScope>(named("ApplicationScope")), get<Uploader>())
    }

    single<DownloadController> {
        CoroutineDownloadController(get<CoroutineScope>(named("ApplicationScope")), get<Downloader>())
    }

    single<ChangesetAutoCloser> {
        CoroutineChangesetAutoCloser(
            get<CoroutineScope>(named("ApplicationScope")),
        ) {
            // Resolve this only when delayed work runs: OpenChangesetsManager itself needs the
            // auto-closer, so eager constructor injection would form a Koin resolution cycle.
            get<OpenChangesetsManager>().closeOldChangesets()
        }
    }

    factory<PeriodicCleaner> { IosPeriodicCleaner() }
}

@OptIn(ExperimentalForeignApi::class)
private fun applicationSupportPath(fileName: String): Path {
    val appSupportUrl = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )!!
    return Path(appSupportUrl.URLByAppendingPathComponent(fileName)!!.path!!)
}
