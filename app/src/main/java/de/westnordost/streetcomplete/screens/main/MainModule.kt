package de.westnordost.streetcomplete.screens.main

import org.koin.dsl.module

val mainModule = module {
    factory { QuestSourceIsSurveyChecker() }
}
