package de.westnordost.streetcomplete.data.osm.osmquests

import org.koin.dsl.module

val osmQuestModule = module {
    factory { OsmQuestDao(get()) }
    factory { OsmQuestsHiddenDao(get()) }

    single<OsmQuestSource> { get<OsmQuestController>() }
}
