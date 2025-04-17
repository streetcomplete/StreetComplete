package de.westnordost.streetcomplete.data.changelog

import org.koin.dsl.module

val changelogModule = module {
    single { Changelog(get()) }
}
