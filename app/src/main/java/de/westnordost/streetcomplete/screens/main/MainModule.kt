package de.westnordost.streetcomplete.screens.main

import de.westnordost.streetcomplete.data.location.RecentLocationStore
import de.westnordost.streetcomplete.screens.main.controls.MessagesButtonViewModel
import de.westnordost.streetcomplete.screens.main.controls.MessagesButtonViewModelImpl
import de.westnordost.streetcomplete.screens.main.controls.OverlaysButtonViewModel
import de.westnordost.streetcomplete.screens.main.controls.OverlaysButtonViewModelImpl
import de.westnordost.streetcomplete.screens.main.controls.UndoButtonViewModel
import de.westnordost.streetcomplete.screens.main.controls.UndoButtonViewModelImpl
import de.westnordost.streetcomplete.screens.main.controls.UploadButtonViewModel
import de.westnordost.streetcomplete.screens.main.controls.UploadButtonViewModelImpl
import de.westnordost.streetcomplete.util.location.LocationAvailabilityReceiver
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    single { LocationAvailabilityReceiver(get()) }
    single { RecentLocationStore() }

    viewModel<UploadButtonViewModel> { UploadButtonViewModelImpl(get(), get(), get(), get(), get(), get()) }
    viewModel<UndoButtonViewModel> { UndoButtonViewModelImpl(get(), get()) }
    viewModel<OverlaysButtonViewModel> { OverlaysButtonViewModelImpl(get(), get(), get()) }
    viewModel<MessagesButtonViewModel> { MessagesButtonViewModelImpl(get()) }
}
