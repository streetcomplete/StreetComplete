package de.westnordost.streetcomplete.data.visiblequests

import org.koin.dsl.module

val questPresetsModule = module {
    factory { QuestPresetsDao(get()) }
    factory<QuestPresetsSource> { get<QuestPresetsController>() }
    factory { QuestTypeOrderDao(get()) }
    factory<QuestTypeOrderSource> { get<QuestTypeOrderController>() }
    factory { VisibleQuestTypeDao(get()) }
    factory<VisibleQuestTypeSource> { get<VisibleQuestTypeController>() }

    single { QuestPresetsController(get(), get()) }
    single { QuestTypeOrderController(get(), get()) }
    single { SelectedQuestPresetStore(get()) }
    single { TeamModeQuestFilter(get(), get()) }
    single { VisibleQuestTypeController(get(), get()) }
}
