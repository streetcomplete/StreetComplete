package de.westnordost.streetcomplete.data.messages

import org.koin.dsl.module

val messagesModule = module {
    single { MessagesSource(get(), get(), get(), get(), get()) }
}
