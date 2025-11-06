package de.westnordost.streetcomplete.quests.artwork

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.artwork.ArtworkType.ARCHITECTURE
import de.westnordost.streetcomplete.quests.artwork.ArtworkType.BUST
import de.westnordost.streetcomplete.quests.artwork.ArtworkType.FOUNTAIN
import de.westnordost.streetcomplete.quests.artwork.ArtworkType.GRAFFITI
import de.westnordost.streetcomplete.quests.artwork.ArtworkType.INSTALLATION
import de.westnordost.streetcomplete.quests.artwork.ArtworkType.LAND_ART
import de.westnordost.streetcomplete.quests.artwork.ArtworkType.MOSAIC
import de.westnordost.streetcomplete.quests.artwork.ArtworkType.MURAL
import de.westnordost.streetcomplete.quests.artwork.ArtworkType.PAINTING
import de.westnordost.streetcomplete.quests.artwork.ArtworkType.RELIEF
import de.westnordost.streetcomplete.quests.artwork.ArtworkType.SCULPTURE
import de.westnordost.streetcomplete.quests.artwork.ArtworkType.STATUE
import de.westnordost.streetcomplete.quests.artwork.ArtworkType.STONE
import de.westnordost.streetcomplete.view.image_select.Item

fun ArtworkType.asItem() = Item(this, iconResId, titleResId)

private val ArtworkType.titleResId: Int get() = when (this) {
    SCULPTURE -> R.string.quest_artwork_sculpture
    STATUE -> R.string.quest_artwork_statue
    BUST -> R.string.quest_artwork_bust
    ARCHITECTURE -> R.string.quest_artwork_architecture
    RELIEF -> R.string.quest_artwork_relief
    MURAL -> R.string.quest_artwork_mural
    FOUNTAIN -> R.string.quest_artwork_fountain
    INSTALLATION -> R.string.quest_artwork_installation
    STONE -> R.string.quest_artwork_stone
    MOSAIC -> R.string.quest_artwork_mosaic
    GRAFFITI -> R.string.quest_artwork_graffiti
    PAINTING -> R.string.quest_artwork_painting
    LAND_ART -> R.string.quest_artwork_land_art
}

private val ArtworkType.iconResId: Int get() = when (this) {
    SCULPTURE -> R.drawable.artwork_type_sculpture
    STATUE -> R.drawable.artwork_type_statue
    BUST -> R.drawable.memorial_type_bust
    ARCHITECTURE -> R.drawable.artwork_type_architecture
    RELIEF -> R.drawable.artwork_type_relief
    MURAL -> R.drawable.artwork_type_mural
    FOUNTAIN -> R.drawable.artwork_type_fountain
    INSTALLATION -> R.drawable.artwork_type_installation
    STONE -> R.drawable.artwork_type_stone
    MOSAIC -> R.drawable.artwork_type_mosaic
    GRAFFITI -> R.drawable.artwork_type_graffiti
    PAINTING -> R.drawable.artwork_type_painting
    LAND_ART -> R.drawable.artwork_type_land_art
}
