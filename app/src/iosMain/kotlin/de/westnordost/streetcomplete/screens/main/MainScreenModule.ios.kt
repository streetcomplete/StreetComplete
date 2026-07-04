package de.westnordost.streetcomplete.screens.main

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

actual val mainScreenPlatformModule = module {

    factory<MapAppLauncher> { IosMapAppLauncher }
    factory<EmailAppLauncher> { IosEmailAppLauncher }
}
