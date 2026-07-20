package de.westnordost.streetcomplete

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.connection.ActiveNetworkConnection
import de.westnordost.streetcomplete.data.connection.IosActiveNetworkConnection
import de.westnordost.streetcomplete.screens.about.AppStoreInfo
import de.westnordost.streetcomplete.screens.about.IosAppStoreInfo
import de.westnordost.streetcomplete.screens.main.EmailAppLauncher
import de.westnordost.streetcomplete.screens.main.IosEmailAppLauncher
import de.westnordost.streetcomplete.screens.main.IosMapAppLauncher
import de.westnordost.streetcomplete.screens.main.MapAppLauncher
import de.westnordost.streetcomplete.ui.util.measure.ArMeasureAppLauncher
import de.westnordost.streetcomplete.ui.util.measure.ArSupportChecker
import de.westnordost.streetcomplete.ui.util.measure.IosArMeasureAppLauncher
import de.westnordost.streetcomplete.ui.util.measure.IosArSupportChecker
import de.westnordost.streetcomplete.ui.util.photo.HasCameraChecker
import de.westnordost.streetcomplete.ui.util.photo.IosHasCameraChecker
import de.westnordost.streetcomplete.util.error_reporting.CrashReportHolder
import de.westnordost.streetcomplete.util.error_reporting.EmptyCrashReportHolder
import de.westnordost.streetcomplete.util.sound.IosSoundEffectPlayer
import de.westnordost.streetcomplete.util.sound.SoundEffectPlayer
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

private val COMPOSE_FILES_DIR = NSBundle.mainBundle.resourcePath + "/compose-resources/files"

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

    // app store info

    single<AppStoreInfo> { IosAppStoreInfo }

    // take photos

    factory<HasCameraChecker>() { IosHasCameraChecker }

    // AR

    factory<ArSupportChecker> { IosArSupportChecker() }
    factory<ArMeasureAppLauncher> { IosArMeasureAppLauncher() }

    // launch apps

    factory<MapAppLauncher> { IosMapAppLauncher }
    factory<EmailAppLauncher> { IosEmailAppLauncher }

    // settings

    single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }

    // sound

    single<SoundEffectPlayer> { IosSoundEffectPlayer(COMPOSE_FILES_DIR) }

    // connection

    factory<ActiveNetworkConnection> { IosActiveNetworkConnection() }
}
