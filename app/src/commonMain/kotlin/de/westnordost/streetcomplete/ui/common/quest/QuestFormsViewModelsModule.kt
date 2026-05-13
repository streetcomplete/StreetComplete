package de.westnordost.streetcomplete.ui.common.quest

import de.westnordost.streetcomplete.screens.about.logs.LogsViewModel
import de.westnordost.streetcomplete.screens.about.logs.LogsViewModelImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val questFormsViewModelsModule = module {
    viewModel { ItemSelectViewModel(get()) }
    viewModel { LocalizedNameViewModel(get()) }
}
