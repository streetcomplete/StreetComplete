package de.westnordost.streetcomplete.data.osmnotes.edits

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module object NoteEditsModule {

    @Provides @Singleton fun noteEditsSource(
        noteEditsController: NoteEditsController
    ): NoteEditsSource = noteEditsController
}
