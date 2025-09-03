package de.westnordost.streetcomplete.ui.ktx

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import platform.Foundation.NSUserDefaults

import org.koin.dsl.module

val iosModule = module {
    // Settings

    single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
}
