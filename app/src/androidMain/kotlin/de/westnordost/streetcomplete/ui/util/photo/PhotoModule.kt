package de.westnordost.streetcomplete.ui.util.photo

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel

val photoModule = module {
    factory<HasCameraChecker>() { AndroidHasCameraChecker(get()) }

    viewModel<PhotosViewModel> { PhotosViewModelImpl(get(), get()) }
}
