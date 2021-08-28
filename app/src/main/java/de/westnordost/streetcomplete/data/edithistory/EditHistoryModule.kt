package de.westnordost.streetcomplete.data.edithistory

import dagger.Module
import dagger.Provides

@Module
object EditHistoryModule {
    @Provides fun editHistorySource(editHistoryController: EditHistoryController): EditHistorySource =
        editHistoryController
}
