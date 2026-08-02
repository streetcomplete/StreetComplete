package de.westnordost.streetcomplete.quests.beer

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.ARadioGroupQuestForm
import de.westnordost.streetcomplete.quests.beer.BeerServed.*
import org.jetbrains.compose.resources.stringResource

class AddBeerForm : ARadioGroupQuestForm<BeerServed, BeerServed>() {

    override val items: List<BeerServed> get() {
        val tags = element.tags
        val isPubOrBar = tags["amenity"] == "pub" || tags["amenity"] == "bar" || tags["amenity"] == "biergarten"

        return if (isPubOrBar) {
            listOf(DRAUGHT, BOTTLED)
        } else {
            listOf(DRAUGHT, BOTTLED, YES, NO)
        }
    }

    @Composable override fun BoxScope.ItemContent(item: BeerServed) {
        Text(stringResource(item.text))
    }
}