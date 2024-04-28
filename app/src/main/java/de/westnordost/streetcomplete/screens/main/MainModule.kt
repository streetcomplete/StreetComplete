package de.westnordost.streetcomplete.screens.main

import de.westnordost.streetcomplete.data.location.RecentLocationStore
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModelImpl
import de.westnordost.streetcomplete.util.location.LocationAvailabilityReceiver
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val mainModule = module {
    single { LocationAvailabilityReceiver(get()) }
    single { RecentLocationStore() }

    viewModel<MainViewModel> { MainViewModelImpl(
        get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()
    ) }

    viewModel<EditHistoryViewModel> { EditHistoryViewModelImpl(get(), get(), get(named("FeatureDictionaryLazy"))) }
}
