package de.westnordost.streetcomplete.data.osmnotes.notequests

import org.koin.dsl.module

val osmNoteQuestModule = module {
    factory { NoteQuestsHiddenDao(get()) }

    single { NotesPreferences(get()) }

    single<OsmNoteQuestSource> { get<OsmNoteQuestController>() }
    single<OsmNoteQuestsHiddenSource> { get<OsmNoteQuestController>() }
    single<OsmNoteQuestsHiddenController> { get<OsmNoteQuestController>() }

    single { OsmNoteQuestController(get(), get(), get(), get(), get()) }
}
