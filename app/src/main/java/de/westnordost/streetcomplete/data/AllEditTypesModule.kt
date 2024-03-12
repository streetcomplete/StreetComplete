package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import org.koin.dsl.module

val allEditTypesModule =  module {
    single {
        AllEditTypes(listOf(
            get<QuestTypeRegistry>(),
            get<OverlayRegistry>()
        ))
    }
}
