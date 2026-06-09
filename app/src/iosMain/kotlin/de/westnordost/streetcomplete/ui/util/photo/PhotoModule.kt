package de.westnordost.streetcomplete.ui.util.photo

import org.koin.dsl.module

val photoModule = module {
    factory<HasCameraChecker>() { IosHasCameraChecker() }
}
