package de.westnordost.streetcomplete.osm.surface

import android.content.res.Resources
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.RotatedCircleDrawable
import de.westnordost.streetcomplete.view.controller.StreetSideDisplayItem
import de.westnordost.streetcomplete.view.controller.StreetSideItem2
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

fun List<Surface>.toItems() = this.map { it.asItem() }

fun Surface.asItem(): DisplayItem<Surface> = Item(this, iconResId, titleResId)

fun Surface.asStreetSideItem(resources: Resources): StreetSideDisplayItem<Surface> =
    StreetSideItem2(
        this,
        ResImage(R.drawable.ic_sidewalk_illustration_yes),
        ResText(titleResId),
        ResImage(iconResId),
        DrawableImage(RotatedCircleDrawable(resources.getDrawable(iconResId)))
    )

