package de.westnordost.streetcomplete.screens.measure

import org.koin.dsl.module

val arModule = module {
    factory { ArSupportChecker(get()) }
}
