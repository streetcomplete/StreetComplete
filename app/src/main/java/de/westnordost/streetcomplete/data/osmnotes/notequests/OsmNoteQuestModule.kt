package de.westnordost.streetcomplete.data.osmnotes.notequests

import dagger.Module
import dagger.Provides

@Module
object OsmNoteQuestModule {
    @Provides fun osmNoteQuestSource(ctrl: OsmNoteQuestController): OsmNoteQuestSource = ctrl
}
