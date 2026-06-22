package de.westnordost.streetcomplete.data.osmnotes.edits

import org.koin.dsl.module

val noteEditsModule = module {
    factory { NoteEditsDao(get()) }

    single { NoteEditsUploader(get(), get(), get(), get(), get(), get(), get()) }
    single<NoteEditsController> { NoteEditsControllerImpl(get()) }
    single<NoteEditsSource> { get<NoteEditsController>() }
    single<NotesWithEditsSource> { NotesWithEditsSourceImpl(get(), get(), get()) }
}
