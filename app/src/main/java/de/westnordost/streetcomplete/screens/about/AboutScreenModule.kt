package de.westnordost.streetcomplete.screens.about

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val aboutScreenModule = module {
    viewModel<LogsViewModel> { LogsViewModelImpl(get()) }
    viewModel<CreditsViewModel> { CreditsViewModelImpl(get()) }
    viewModel<ChangelogViewModel> { ChangelogViewModelImpl(get()) }
}
