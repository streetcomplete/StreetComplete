package de.westnordost.streetcomplete.quests.artwork

import de.westnordost.streetcomplete.quests.artwork.ArtworkType.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.resources.Res
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val ArtworkType.title: StringResource get() = when (this) {
    SCULPTURE -> Res.string.quest_artwork_sculpture
    STATUE -> Res.string.quest_artwork_statue
    BUST -> Res.string.quest_artwork_bust
    ARCHITECTURE -> Res.string.quest_artwork_architecture
    RELIEF -> Res.string.quest_artwork_relief
    MURAL -> Res.string.quest_artwork_mural
    FOUNTAIN -> Res.string.quest_artwork_fountain
    INSTALLATION -> Res.string.quest_artwork_installation
    STONE -> Res.string.quest_artwork_stone
    MOSAIC -> Res.string.quest_artwork_mosaic
    GRAFFITI -> Res.string.quest_artwork_graffiti
    PAINTING -> Res.string.quest_artwork_painting
    STREET_ART -> Res.string.quest_artwork_street_art
}

val ArtworkType.icon: DrawableResource get() = when (this) {
    SCULPTURE -> Res.drawable.artwork_sculpture
    STATUE -> Res.drawable.artwork_statue
    BUST -> Res.drawable.artwork_bust
    ARCHITECTURE -> Res.drawable.artwork_architecture
    RELIEF -> Res.drawable.artwork_relief
    MURAL -> Res.drawable.artwork_mural
    FOUNTAIN -> Res.drawable.artwork_fountain
    INSTALLATION -> Res.drawable.artwork_installation
    STONE -> Res.drawable.artwork_stone
    MOSAIC -> Res.drawable.artwork_mosaic
    GRAFFITI -> Res.drawable.artwork_graffiti
    PAINTING -> Res.drawable.artwork_painting
    STREET_ART -> Res.drawable.artwork_street_art
}
