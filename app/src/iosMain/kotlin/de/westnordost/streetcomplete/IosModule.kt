package de.westnordost.streetcomplete

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.DatabaseImpl
import de.westnordost.streetcomplete.data.StreetCompleteDatabaseConfigurator
import de.westnordost.streetcomplete.data.connection.ActiveNetworkConnection
import de.westnordost.streetcomplete.data.connection.IosActiveNetworkConnection
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.download.IosDownloadController
import de.westnordost.streetcomplete.data.initialize
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetAutoCloser
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.IosChangesetAutoCloser
import de.westnordost.streetcomplete.data.upload.IosUploadController
import de.westnordost.streetcomplete.data.upload.UploadController
import de.westnordost.streetcomplete.screens.about.AppStoreInfo
import de.westnordost.streetcomplete.screens.about.IosAppStoreInfo
import de.westnordost.streetcomplete.screens.main.EmailAppLauncher
import de.westnordost.streetcomplete.screens.main.IosEmailAppLauncher
import de.westnordost.streetcomplete.screens.main.IosMapAppLauncher
import de.westnordost.streetcomplete.screens.main.MapAppLauncher
import de.westnordost.streetcomplete.ui.util.measure.ArSupportChecker
import de.westnordost.streetcomplete.ui.util.measure.IosArSupportChecker
import de.westnordost.streetcomplete.util.error_reporting.CrashReportHolder
import de.westnordost.streetcomplete.util.error_reporting.EmptyCrashReportHolder
import de.westnordost.streetcomplete.util.sound.IosSoundEffectPlayer
import de.westnordost.streetcomplete.util.sound.SoundEffectPlayer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.maplibre.compose.location.IosLocationProvider
import org.maplibre.compose.location.IosSystemSettingsLauncher
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.SystemSettingsLauncher
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSBundle
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask

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
        val appSupportUrl = NSFileManager.defaultManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )!!
        val databaseUrl = appSupportUrl.URLByAppendingPathComponent(ApplicationConstants.DATABASE_NAME)!!
        val databaseFilePath = databaseUrl.path!!
        val databaseConnection = BundledSQLiteDriver().open(databaseFilePath)
        DatabaseImpl(databaseConnection).apply { initialize(StreetCompleteDatabaseConfigurator) }
    }

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

    factory<LocationProvider> { IosLocationProvider() }
    factory<SystemSettingsLauncher> { IosSystemSettingsLauncher() }

    // launch apps

    factory<MapAppLauncher> { IosMapAppLauncher }
    factory<EmailAppLauncher> { IosEmailAppLauncher }

    // settings

    single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }

    // sound

    single<SoundEffectPlayer> { IosSoundEffectPlayer(COMPOSE_FILES_DIR) }

    // connection

    factory<ActiveNetworkConnection> { IosActiveNetworkConnection() }

    // background jobs

    single<UploadController> { IosUploadController() }

    single<DownloadController> { IosDownloadController() }

    factory<ChangesetAutoCloser> { IosChangesetAutoCloser() }
}
