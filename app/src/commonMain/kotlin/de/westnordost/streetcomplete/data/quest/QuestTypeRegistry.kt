package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.ObjectTypeRegistry

/** Every osm quest needs to be registered here.
 *
 * Could theoretically be done with Reflection, but that doesn't really work on Android.
 *
 * It is also used to define a (display) order of the quest types and to assign an ordinal to each
 * quest type for serialization.
 */
class QuestTypeRegistry(ordinalsAndEntries: List<Pair<Int, QuestType>>) : ObjectTypeRegistry<QuestType>(ordinalsAndEntries)
