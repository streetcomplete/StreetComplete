package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.atp.atpquests.edits.AtpDataWithEditsSource
import de.westnordost.streetcomplete.data.atp.atpquests.edits.AtpEditsSource
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import org.koin.dsl.module

val atpEditsModule = module {
    single { AtpDataWithEditsSource(get(), get(), get()) }
    single { AtpEditsController() }
    single<AtpEditsSource> { get<AtpEditsController>() }
}
