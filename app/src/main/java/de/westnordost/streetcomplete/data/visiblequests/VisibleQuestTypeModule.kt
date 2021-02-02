package de.westnordost.streetcomplete.data.visiblequests

import dagger.Module
import dagger.Provides

@Module object VisibleQuestTypeModule {
    @Provides fun visibleQuestTypeSource(ctrl: VisibleQuestTypeController): VisibleQuestTypeSource = ctrl
}
