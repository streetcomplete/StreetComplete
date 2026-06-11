package de.westnordost.streetcomplete.screens.about

import org.koin.core.module.Module
import org.koin.dsl.module

actual val aboutScreenPlatformModule = module {
    single<AppStoreInfo> { AndroidAppStoreInfo(get()) }
}
