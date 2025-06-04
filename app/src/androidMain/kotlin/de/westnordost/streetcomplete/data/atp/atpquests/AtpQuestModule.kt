package de.westnordost.streetcomplete.data.atp.atpquests

import de.westnordost.streetcomplete.data.atp.atpquests.AtpQuestController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenDao
import org.koin.dsl.module

val atpQuestModule = module {
    factory { AtpQuestsHiddenDao(get()) }

    single<AtpQuestSource> { get<AtpQuestController>() }

    single { AtpQuestController(get(), get(), get(), get()) }
}
