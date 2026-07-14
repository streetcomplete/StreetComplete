package de.westnordost.streetcomplete

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.data.connection.ActiveNetworkConnection
import de.westnordost.streetcomplete.data.connection.IosActiveNetworkConnection
import de.westnordost.streetcomplete.util.sound.IosSoundEffectPlayer
import de.westnordost.streetcomplete.util.sound.SoundEffectPlayer
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

val iosModule = module {

    // Settings

    single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }

    // sound

    single<SoundEffectPlayer> {
        val dir = NSBundle.mainBundle.resourcePath + "/compose-resources/files"
        IosSoundEffectPlayer(dir)
    }

    factory<ActiveNetworkConnection> { IosActiveNetworkConnection() }
}
