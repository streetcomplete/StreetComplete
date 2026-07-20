package de.westnordost.streetcomplete

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.data.connection.ActiveNetworkConnection
import de.westnordost.streetcomplete.data.connection.IosActiveNetworkConnection
import de.westnordost.streetcomplete.screens.main.EmailAppLauncher
import de.westnordost.streetcomplete.screens.main.IosEmailAppLauncher
import de.westnordost.streetcomplete.screens.main.IosMapAppLauncher
import de.westnordost.streetcomplete.screens.main.MapAppLauncher
import de.westnordost.streetcomplete.util.error_reporting.CrashReportHolder
import de.westnordost.streetcomplete.util.error_reporting.EmptyCrashReportHolder
import de.westnordost.streetcomplete.util.sound.IosSoundEffectPlayer
import de.westnordost.streetcomplete.util.sound.SoundEffectPlayer
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

private val COMPOSE_FILES_DIR = NSBundle.mainBundle.resourcePath + "/compose-resources/files"

val iosModule = module {

    // error reporting

    single<CrashReportHolder> { EmptyCrashReportHolder }

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
