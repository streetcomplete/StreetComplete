package de.westnordost.streetcomplete.data.visiblequests

import org.koin.dsl.module

val visibleQuestsModule = module {
    factory { QuestTypeOrderDao(get()) }
    factory { VisibleEditTypeDao(get()) }

    single<QuestTypeOrderSource> { get<QuestTypeOrderController>() }
    single { QuestTypeOrderController(get(), get(), get()) }

    single { TeamModeQuestFilter(get(), get()) }
    single { LevelFilter(get()) }
    single { DayNightQuestFilter(get()) }

    single<QuestsHiddenSource> { get<QuestsHiddenController>() }
    single { QuestsHiddenController(get(), get(), get()) }

    single<VisibleEditTypeSource> { get<VisibleEditTypeController>() }
    single { VisibleEditTypeController(get(), get(), get()) }
}
