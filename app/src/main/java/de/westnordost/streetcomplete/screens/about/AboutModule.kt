package de.westnordost.streetcomplete.screens.about

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val aboutModule = module {
    viewModel { LogsViewModel(get()) }
}
