package de.westnordost.streetcomplete.screens.settings

import de.westnordost.streetcomplete.screens.settings.questselection.QuestSelectionViewModel
import de.westnordost.streetcomplete.screens.settings.questselection.QuestSelectionViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val settingsModule = module {
    single { ResurveyIntervalsUpdater(get()) }

    viewModel<SettingsViewModel> { SettingsViewModelImpl(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel<QuestSelectionViewModel> { QuestSelectionViewModelImpl(get(), get(), get(), get(), get(named("CountryBoundariesLazy")), get()) }
}
