package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuest
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Access and listen to undoable changes there are.
 * */
@Singleton class UndoableOsmQuestsSource @Inject constructor(){

    private val listeners: MutableList<UndoableOsmQuestsCountListener> = CopyOnWriteArrayList()

    // TODO reimplement this class!

    var count: Int = 0


    /** Get the last undoable quest (includes answered, hidden and uploaded) */
    fun getLastUndoable(): OsmQuest? = null

    fun addListener(listener: UndoableOsmQuestsCountListener) {
        listeners.add(listener)
    }
    fun removeListener(listener: UndoableOsmQuestsCountListener) {
        listeners.remove(listener)
    }
}

interface UndoableOsmQuestsCountListener {
    fun onUndoableOsmQuestsCountIncreased()
    fun onUndoableOsmQuestsCountDecreased()
}
