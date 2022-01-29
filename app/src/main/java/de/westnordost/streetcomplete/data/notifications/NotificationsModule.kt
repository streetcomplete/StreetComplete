package de.westnordost.streetcomplete.data.notifications

import org.koin.dsl.module

val notificationsModule = module {
    single { NotificationsSource(get(), get(), get(), get()) }
    single { QuestSelectionHintController(get(), get()) }
}
