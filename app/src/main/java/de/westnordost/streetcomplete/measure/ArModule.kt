package de.westnordost.streetcomplete.measure

import org.koin.dsl.module

val arModule = module {
    factory { ArSupportChecker(get()) }
}
