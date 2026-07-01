package de.westnordost.streetcomplete.screens.about

import de.westnordost.streetcomplete.screens.about.logs.LogsViewModel
import de.westnordost.streetcomplete.screens.about.logs.LogsViewModelImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.ParametersHolder
import org.koin.core.scope.Scope
import org.koin.dsl.module

val aboutScreenModule = module {
    viewModel<LogsViewModel> { LogsViewModelImpl(get()) }
    viewModel<CreditsViewModel> { CreditsViewModelImpl(get()) }
    viewModel<ChangelogViewModel> { ChangelogViewModelImpl(get()) }
    includes(aboutScreenPlatformModule)
}

expect val aboutScreenPlatformModule: Module
