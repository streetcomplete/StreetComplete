package de.westnordost.streetcomplete.screens.about

import org.koin.dsl.module

actual val aboutScreenPlatformModule = module {
    single<AppStoreInfo> { IosAppStoreInfo() }
}
