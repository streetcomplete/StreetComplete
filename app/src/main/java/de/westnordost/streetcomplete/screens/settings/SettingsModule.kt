package de.westnordost.streetcomplete.screens.settings

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    single { ResurveyIntervalsUpdater(get()) }

    viewModel<SettingsViewModel> { SettingsViewModelImpl(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}
