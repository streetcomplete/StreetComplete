package de.westnordost.streetcomplete

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.DatabaseImpl
import de.westnordost.streetcomplete.data.StreetCompleteDatabaseConfigurator
import de.westnordost.streetcomplete.data.connection.ActiveNetworkConnection
import de.westnordost.streetcomplete.data.connection.DesktopActiveNetworkConnection
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
import de.westnordost.streetcomplete.data.upload.UploadController
import de.westnordost.streetcomplete.data.upload.Uploader
import de.westnordost.streetcomplete.screens.about.AppStoreInfo
import de.westnordost.streetcomplete.ui.util.measure.ArSupportChecker
import de.westnordost.streetcomplete.util.error_reporting.CrashReportHolder
import de.westnordost.streetcomplete.util.error_reporting.DesktopCrashReportHolder
import de.westnordost.streetcomplete.util.sound.DesktopSoundEffectPlayer
import de.westnordost.streetcomplete.util.sound.SoundEffectPlayer
import java.awt.GraphicsEnvironment
import java.io.File
import java.util.prefs.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.maplibre.compose.desktop.DesktopRuntimeOptions
import org.maplibre.compose.location.DesktopSystemSettingsLauncher
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.SystemSettingsLauncher
import org.maplibre.compose.location.createDefaultLocationProvider
import org.maplibre.compose.map.MapRuntime
import org.maplibre.compose.map.createMapRuntime

val desktopModule = module {
    single<de.westnordost.countryboundaries.CountryBoundaries> {
        val source = SystemFileSystem
            .source(Path(desktopFilesDirectory().path, "boundaries.ser"))
            .buffered()
        de.westnordost.countryboundaries.CountryBoundaries.deserializeFrom(source)
    }

    single<FeatureDictionary> {
        val files = desktopFilesDirectory().path
        FeatureDictionary.create(
            fileSystem = SystemFileSystem,
            presetsBasePath = File(files, "osmfeatures/default").path,
            brandPresetsBasePath = File(files, "osmfeatures/brands").path,
        )
    }

    single<Database> {
        val databasePath = Path(desktopDataDirectory(), ApplicationConstants.DATABASE_NAME)
        val connection = BundledSQLiteDriver().open(databasePath.toString())
        DatabaseImpl(connection).apply { initialize(StreetCompleteDatabaseConfigurator) }
    } onClose { it?.close() }

    factory(named("AvatarsCacheDirectory")) {
        val path = Path(desktopCacheDirectory(), ApplicationConstants.AVATARS_CACHE_DIRECTORY)
        SystemFileSystem.createDirectories(path, mustCreate = false)
        path
    }

    single<AppStoreInfo> {
        object : AppStoreInfo {
            // TODO(multiplatform): Add a rating URI if the desktop build is published through a
            // store with a review page. The current direct distributable has no such destination.
            override fun getRatingUri(): String? = null
            override fun disallowsInAppDonationLinks(): Boolean = false
        }
    }
    factory<ArSupportChecker> {
        object : ArSupportChecker {
            // TODO(multiplatform): Enable when StreetMeasure defines a desktop launch/result
            // protocol. The existing AR feature depends on its Android-only application contract.
            override fun invoke(): Boolean = false
        }
    }
    single<ObservableSettings> {
        PreferencesSettings(Preferences.userRoot().node("de/westnordost/streetcomplete"))
    }

    single<DesktopSoundEffectPlayer> { DesktopSoundEffectPlayer() } onClose { it?.close() }
    single<SoundEffectPlayer> { get<DesktopSoundEffectPlayer>() }

    single<DesktopCrashReportHolder> {
        DesktopCrashReportHolder(
            File(desktopDataDirectory().toString(), "crashreport.txt"),
            get(),
        ).apply { install() }
    }
    single<CrashReportHolder> { get<DesktopCrashReportHolder>() }

    single<ActiveNetworkConnection> {
        DesktopActiveNetworkConnection(get(named("ApplicationScope")))
    }

    single<LocationProvider> { createDefaultLocationProvider() } onClose {
        (it as? AutoCloseable)?.close()
    }
    factory<SystemSettingsLauncher> { DesktopSystemSettingsLauncher() }

    single<MapRuntime> {
        createMapRuntime(DesktopRuntimeOptions(applicationId = "de.westnordost.streetcomplete"))
    } onClose { it?.close() }
    factory<MapTilesDownloader> {
        val transform = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .defaultScreenDevice
            .defaultConfiguration
            .defaultTransform
        MapLibreMapTilesDownloader(get(), transform.scaleX.toFloat())
    }

    single<UploadController> {
        CoroutineUploadController(get(named("ApplicationScope")), get<Uploader>())
    }
    single<DownloadController> {
        CoroutineDownloadController(get(named("ApplicationScope")), get<Downloader>())
    }
    single<ChangesetAutoCloser> {
        CoroutineChangesetAutoCloser(get(named("ApplicationScope"))) {
            get<OpenChangesetsManager>().closeOldChangesets()
        }
    }
}
