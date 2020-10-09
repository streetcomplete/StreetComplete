package de.westnordost.streetcomplete.quests.max_weight

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.image_select.BitmapImage
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item2

fun MaxWeightSign.asItem(inflater: LayoutInflater): DisplayItem<MaxWeightSign> =
    Item2(this, BitmapImage(createBitmap(inflater)))

val MaxWeightSign.layoutResourceId get() = when(this) {
    MaxWeightSign.MAX_WEIGHT             -> R.layout.quest_maxweight_sign
    MaxWeightSign.MAX_GROSS_VEHICLE_MASS -> R.layout.quest_maxweight_mgv_sign
    MaxWeightSign.MAX_AXLE_LOAD          -> R.layout.quest_maxweight_axleload_sign
    MaxWeightSign.MAX_TANDEM_AXLE_LOAD   -> R.layout.quest_maxweight_tandem_axleload_sign
}

private fun MaxWeightSign.createBitmap(inflater: LayoutInflater): Bitmap {
    val container = FrameLayout(inflater.context)
    container.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    inflater.inflate(layoutResourceId, container)

    container.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    val w = container.measuredWidth
    val h = container.measuredHeight
    container.layout(0,0, w, h)
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    container.draw(canvas)
    return bitmap
}
