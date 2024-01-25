package de.westnordost.streetcomplete.data.externalsource

import org.koin.core.qualifier.named
import org.koin.dsl.module

val externalSourceModule = module {
    single { ExternalSourceQuestController(get(named("CountryBoundariesLazy")), get(), get(), get()) }
    single { ExternalSourceDao(get()) }
}

// todo: if a quest doesn't lead to an elementEdit, currently there is no way to undo
//  -> implement for things like undoing false positive in osmose quest
/*
class ExternalSourceEditKey(val source: String, val id: Long) : EditKey() // have a key for each source that needs it?
data class ExternalSourceEdit(
    override val position: LatLon,
    override val isSynced: Boolean?,
    val action: Unit, // depending on the source and what was done, need some type...
) : Edit {
    override val key: ExternalSourceEditKey
    override val createdTimestamp: Long
    override val isUndoable: Boolean
}
*/
