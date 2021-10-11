package de.westnordost.streetcomplete.data.visiblequests

import dagger.Module
import dagger.Provides

@Module object QuestPresetsModule {
    @Provides fun visibleQuestTypeSource(ctrl: VisibleQuestTypeController): VisibleQuestTypeSource = ctrl

    @Provides fun questTypeOrderSource(ctrl: QuestTypeOrderController): QuestTypeOrderSource = ctrl

    @Provides fun questPresetsSource(ctrl: QuestPresetsController): QuestPresetsSource = ctrl
}
