package de.westnordost.streetcomplete.data.othersource

import org.koin.core.qualifier.named
import org.koin.dsl.module

val otherSourceModule = module {
    single { OtherSourceQuestController(get(named("CountryBoundariesFuture")), get(), get(), get()) }
    single { OtherSourceDao(get()) }
}

// todo: if a quest doesn't lead to an elementEdit, currently there is no way to undo
//  -> implement for things like undo reportFalsePositive
/*
class OtherSourceEditKey(val source: String, val id: Long) : EditKey() // have a key for each source that needs it?
data class OtherSourceEdit(
    override val position: LatLon,
    override val isSynced: Boolean?,
    val action: Unit, // depending on the source and what was done, need some type...
) : Edit {
    override val key: OtherSourceEditKey
    override val createdTimestamp: Long
    override val isUndoable: Boolean
}
*/
