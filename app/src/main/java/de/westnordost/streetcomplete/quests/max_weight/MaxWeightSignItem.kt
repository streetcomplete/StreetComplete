package de.westnordost.streetcomplete.quests.max_weight

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item2

fun MaxWeightSign.asItem(
    inflater: LayoutInflater,
    countryCode: String,
): DisplayItem<MaxWeightSign> = Item2(
    this,
    DrawableImage(BitmapDrawable(
        inflater.context.resources,
        createBitmap(inflater, countryCode)
    )),
)

private fun MaxWeightSign.createBitmap(inflater: LayoutInflater, countryCode: String): Bitmap {
    val container = FrameLayout(inflater.context)
    container.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    inflater.inflate(getLayoutResourceId(countryCode), container)

    container.findViewById<View?>(R.id.genericProhibitionSign)
        ?.setBackgroundResource(getSignBackgroundDrawableResId(countryCode))

    container.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    val w = container.measuredWidth
    val h = container.measuredHeight
    container.layout(0, 0, w, h)
    return createBitmap(w, h, Bitmap.Config.ARGB_8888).applyCanvas {
        container.draw(this)
    }
}

fun MaxWeightSign.getLayoutResourceId(countryCode: String) = when (this) {
    MaxWeightSign.MAX_WEIGHT             -> getMaxWeightSignLayoutResId(countryCode)
    MaxWeightSign.MAX_GROSS_VEHICLE_MASS -> getMaxWeightMgvSignLayoutResId(countryCode)
    MaxWeightSign.MAX_AXLE_LOAD          -> getMaxWeightAxleLoadSignLayoutResId(countryCode)
    MaxWeightSign.MAX_TANDEM_AXLE_LOAD   -> getMaxWeightTandemAxleLoadSignLayoutResId(countryCode)
}

private fun getMaxWeightSignLayoutResId(countryCode: String): Int = when (countryCode) {
    "AU", "CA", "US" -> R.layout.quest_maxweight_sign_us
    else ->             R.layout.quest_maxweight_sign
}

private fun getMaxWeightMgvSignLayoutResId(countryCode: String): Int = when (countryCode) {
    "AU", "CA", "US" -> R.layout.quest_maxweight_mgv_sign_us
    "DE" ->             R.layout.quest_maxweight_mgv_sign_de
    "GB" ->             R.layout.quest_maxweight_mgv_sign_gb
    else ->             R.layout.quest_maxweight_mgv_sign
}

private fun getMaxWeightAxleLoadSignLayoutResId(countryCode: String): Int = when (countryCode) {
    "AU", "CA", "US" -> R.layout.quest_maxweight_axleload_sign_us
    else ->             R.layout.quest_maxweight_axleload_sign
}

private fun getMaxWeightTandemAxleLoadSignLayoutResId(countryCode: String): Int = when (countryCode) {
    "AU", "CA", "US" -> R.layout.quest_maxweight_tandem_axleload_sign_us
    else ->             R.layout.quest_maxweight_tandem_axleload_sign
}

private fun getSignBackgroundDrawableResId(countryCode: String): Int = when (countryCode) {
    "FI", "IS", "SE" -> R.drawable.background_generic_prohibition_sign_yellow
    else ->             R.drawable.background_generic_prohibition_sign
}
