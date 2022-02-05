package de.westnordost.streetcomplete.data.osm.osmquests

import org.koin.core.qualifier.named
import org.koin.dsl.module

val osmQuestModule = module {
    factory { OsmQuestDao(get()) }
    factory<OsmQuestSource> { get<OsmQuestController>() }
    factory { OsmQuestsHiddenDao(get()) }

    single { OsmQuestController(get(), get(), get(), get(), get(), get(named("CountryBoundariesFuture"))) }
}
