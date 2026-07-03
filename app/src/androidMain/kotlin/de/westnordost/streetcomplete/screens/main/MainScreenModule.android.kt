package de.westnordost.streetcomplete.screens.main

import de.westnordost.streetcomplete.util.location.LocationAvailabilityReceiver
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

actual val mainScreenPlatformModule = module {
    single { LocationAvailabilityReceiver(get()) }

    factory<MapAppLauncher> { AndroidMapAppLauncher(get()) }

    viewModel<MainViewModel> { MainViewModelImpl(
        get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
        get(), get(), get(), get(), get(), get(), get()
    ) }
}
