package de.westnordost.streetcomplete.data.overlays

import de.westnordost.streetcomplete.data.ObjectTypeRegistry
import de.westnordost.streetcomplete.overlays.Overlay

/** Every overlay must be registered here
 *
 * Could theoretically be done with Reflection, but that doesn't really work on Android.
 *
 * It is also used to assign each overlay an ordinal for serialization.
 */
class OverlayRegistry(ordinalsAndEntries: List<Pair<Int, Overlay>>) : ObjectTypeRegistry<Overlay>(ordinalsAndEntries)
