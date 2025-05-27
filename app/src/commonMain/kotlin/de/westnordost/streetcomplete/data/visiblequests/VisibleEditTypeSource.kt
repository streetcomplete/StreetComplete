package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.osm.edits.EditType

interface VisibleEditTypeSource {

    /** interface to be notified of changed in edit type visibilities */
    interface Listener {
        fun onVisibilityChanged(editType: EditType, visible: Boolean)
        /** Called when a number of edit type visibilities changed */
        fun onVisibilitiesChanged()
    }

    /** return whether the given edit type is visible */
    fun isVisible(editType: EditType): Boolean

    fun getVisible(presetId: Long? = null): Set<EditType>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
