package de.westnordost.streetcomplete.data.visiblequests

import org.koin.dsl.module

val visibleQuestsModule = module {
    factory { QuestTypeOrderDao(get()) }
    factory { VisibleEditTypeDao(get()) }

    single<QuestTypeOrderSource> { get<QuestTypeOrderController>() }
    single { QuestTypeOrderController(get(), get(), get()) }

    single { TeamModeQuestFilter(get(), get()) }

    single<QuestsHiddenSource> { get<QuestsHiddenController>() }
    single { QuestsHiddenController(get(), get()) }

    single<VisibleEditTypeSource> { get<VisibleEditTypeController>() }
    single { VisibleEditTypeController(get(), get(), get()) }
}
