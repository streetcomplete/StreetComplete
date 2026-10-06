package de.westnordost.streetcomplete

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.Cleaner
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.DatabaseImpl
import de.westnordost.streetcomplete.data.PeriodicCleaner
import de.westnordost.streetcomplete.data.StreetCompleteDatabaseConfigurator
import de.westnordost.streetcomplete.data.connection.ActiveNetworkConnection
import de.westnordost.streetcomplete.data.connection.NetworkCapabilities
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.download.Downloader
import de.westnordost.streetcomplete.data.initialize
import de.westnordost.streetcomplete.data.maptiles.MapLibreMapTilesDownloader
import de.westnordost.streetcomplete.data.maptiles.MapTilesDownloader
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.upload.UploadController
import de.westnordost.streetcomplete.screens.about.AppStoreInfo
import de.westnordost.streetcomplete.ui.util.measure.ArSupportChecker
import de.westnordost.streetcomplete.util.error_reporting.CrashReportHolder
import de.westnordost.streetcomplete.util.error_reporting.EmptyCrashReportHolder
import de.westnordost.streetcomplete.util.sound.SoundEffectPlayer
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.createDefaultLocationProvider
import org.maplibre.compose.map.MapRuntime
import org.maplibre.compose.map.MapRuntimeOptions
import org.maplibre.compose.map.createMapRuntime
import java.awt.GraphicsEnvironment
import java.util.prefs.Preferences

/** Services for the local development launcher; metadata is read directly from the checkout. */
val desktopModule = module {
    val resourcesDir = Path(requireNotNull(System.getProperty("streetcomplete.resources")))

    // metadata

    single<de.westnordost.countryboundaries.CountryBoundaries> {
        SystemFileSystem.source(Path(resourcesDir, "boundaries.ser")).buffered().use {
            de.westnordost.countryboundaries.CountryBoundaries.deserializeFrom(it)
        }
    }

    single<FeatureDictionary> {
        FeatureDictionary.create(
            fileSystem = SystemFileSystem,
            presetsBasePath = Path(resourcesDir, "osmfeatures/default").toString(),
            brandPresetsBasePath = Path(resourcesDir, "osmfeatures/brands").toString(),
        )
    }

    // error reporting

    single<CrashReportHolder> { EmptyCrashReportHolder }

    // database

    single<Database> {
        val databaseFile = Path(FileKit.filesDir.path, ApplicationConstants.DATABASE_NAME)
        DatabaseImpl(BundledSQLiteDriver().open(databaseFile.toString())).apply {
            initialize(StreetCompleteDatabaseConfigurator)
        }
    } onClose { it?.close() }

    // avatars cache dir

    factory(named("AvatarsCacheDirectory")) {
        Path(FileKit.cacheDir.path, ApplicationConstants.AVATARS_CACHE_DIRECTORY)
    }

    // app store info

    single<AppStoreInfo> { object : AppStoreInfo {
        override fun getRatingUri(): String? = null
        override fun disallowsInAppDonationLinks(): Boolean = false
    } }

    // AR

    factory<ArSupportChecker> { object : ArSupportChecker {
        override fun invoke(): Boolean = false
    } }

    // location

    single<LocationProvider> { createDefaultLocationProvider() } onClose { it?.close() }

    // settings

    single<ObservableSettings> {
        PreferencesSettings(Preferences.userRoot().node("de/westnordost/streetcomplete/desktop"))
    }

    // sound

    single<SoundEffectPlayer> { object : SoundEffectPlayer {
        override fun play(resourcePath: String) {}
    } }

    // connection

    single<ActiveNetworkConnection> { object : ActiveNetworkConnection {
        override val capabilities = flowOf(NetworkCapabilities(hasInternet = true, isMetered = false))
    } }

    // map

    single<MapRuntime> {
        createMapRuntime(MapRuntimeOptions(cacheFile = Path(FileKit.filesDir.path, "maplibre-cache.db")))
    } onClose { it?.close() }

    factory<MapTilesDownloader> {
        val density = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .defaultScreenDevice.defaultConfiguration.defaultTransform.scaleX.toFloat()
        MapLibreMapTilesDownloader(get<MapRuntime>().offlineManager, density)
    }

    // background jobs

    // Login is disabled; edits remain in the local database for inspecting quest behavior.
    single<UploadController> { object : UploadController {
        override fun upload(isUserInitiated: Boolean) {}
    } }

    single<DownloadController> {
        val downloader = get<Downloader>()
        // Downloader already logs errors and notifies its listeners
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main + CoroutineExceptionHandler { _, _ -> })
        var job: Job? = null
        object : DownloadController {
            override fun download(bbox: BoundingBox, isUserInitiated: Boolean) {
                scope.launch {
                    // like on Android: a user-initiated download replaces the current one, others don't
                    val previous = job
                    if (!isUserInitiated && previous?.isActive == true) return@launch
                    job = currentCoroutineContext().job
                    previous?.cancelAndJoin()
                    downloader.download(bbox, isUserInitiated)
                }
            }
        }
    }

    single<PeriodicCleaner> { object : PeriodicCleaner {
        override fun enqueue() { get<Cleaner>().cleanOld() }
    } }
}
