package de.westnordost.streetcomplete.ui.util.photo

import org.koin.dsl.module

actual val photoPlatformModule = module {
    factory<HasCameraChecker>() { AndroidHasCameraChecker(get()) }
}
