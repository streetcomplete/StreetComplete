package de.westnordost.streetcomplete.quests.beer

import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.StringResource

enum class BeerServed(val osmValue: String) {
    DRAUGHT("draught"),
    BOTTLED("bottled"),
    YES("yes"),
    NO("no")
}

val BeerServed.text: StringResource get() = when (this) {
    BeerServed.DRAUGHT -> Res.string.quest_drink_beer_draught
    BeerServed.BOTTLED -> Res.string.quest_drink_beer_bottled
    BeerServed.YES -> Res.string.quest_drink_beer_yes
    BeerServed.NO -> Res.string.quest_drink_beer_no
}