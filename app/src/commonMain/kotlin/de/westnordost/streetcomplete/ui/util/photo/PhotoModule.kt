package de.westnordost.streetcomplete.ui.util.photo

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val photoModule = module {
    viewModel<PhotosViewModel> { PhotosViewModelImpl(get(), get()) }
    includes(photoPlatformModule)
}

expect val photoPlatformModule: Module
