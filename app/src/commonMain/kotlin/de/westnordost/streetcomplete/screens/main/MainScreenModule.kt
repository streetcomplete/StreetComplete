package de.westnordost.streetcomplete.screens.main

import de.westnordost.streetcomplete.data.location.SurveyChecker
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModelImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val mainScreenModule = module {

    single { SurveyChecker() }

    viewModel<EditHistoryViewModel> { EditHistoryViewModelImpl(get(), get(), get(named("FeatureDictionaryLazy"))) }

    includes(mainScreenPlatformModule)
}

expect val mainScreenPlatformModule: Module
