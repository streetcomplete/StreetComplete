package de.westnordost.streetcomplete.data.osmnotes.notequests

import org.koin.dsl.module

val osmNoteQuestModule = module {
    factory { NoteQuestsHiddenDao(get()) }
    factory { NotesPreferences(get()) }
    factory<OsmNoteQuestSource> { get<OsmNoteQuestController>() }

    single { OsmNoteQuestController(get(), get(), get(), get(), get()) }
}
