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
fun MaxWeightSign.getSignType(countryCode: String) = when (this) {
    MaxWeightSign.MAX_WEIGHT             -> getMaxWeightSignType(countryCode)
    MaxWeightSign.MAX_GROSS_VEHICLE_MASS -> getMaxWeightMgvSignType(countryCode)
    MaxWeightSign.MAX_AXLE_LOAD          -> getMaxWeightAxleLoadSignType(countryCode)
    MaxWeightSign.MAX_TANDEM_AXLE_LOAD   -> getMaxWeightTandemAxleLoadSignType(countryCode)
}
fun MaxWeightSign.getLayoutResourceId(countryCode: String) = when (this) {
    MaxWeightSign.MAX_WEIGHT             -> getMaxWeightSignLayoutResId(countryCode)
    MaxWeightSign.MAX_GROSS_VEHICLE_MASS -> getMaxWeightMgvSignLayoutResId(countryCode)
    MaxWeightSign.MAX_AXLE_LOAD          -> getMaxWeightAxleLoadSignLayoutResId(countryCode)
    MaxWeightSign.MAX_TANDEM_AXLE_LOAD   -> getMaxWeightTandemAxleLoadSignLayoutResId(countryCode)
}
enum class MaxWeightSignCountyType {
    US_TYPE,
    FINISH_TYPE,
    GERMAN_TYPE,
    BRITISH_TYPE,
    GENERAL_TYPE,
}


private fun getMaxWeightSignType(countryCode: String): MaxWeightSignCountyType = when ( countryCode) {
    "AU", "CA", "US" -> MaxWeightSignCountyType.US_TYPE
    "FI", "IS", "SE" -> MaxWeightSignCountyType.FINISH_TYPE
    else ->             MaxWeightSignCountyType.GENERAL_TYPE
}
private fun getMaxWeightSignLayoutResId(countryCode: String): Int = when (countryCode) {
    "AU", "CA", "US" -> R.layout.quest_maxweight_sign_us
    "FI", "IS", "SE" -> R.layout.quest_maxweight_sign_fi
    else ->             R.layout.quest_maxweight_sign
}

private fun getMaxWeightMgvSignType(countryCode: String): MaxWeightSignCountyType = when ( countryCode) {
    "AU", "CA", "US" -> MaxWeightSignCountyType.US_TYPE
    "FI", "IS", "SE" -> MaxWeightSignCountyType.FINISH_TYPE
    "DE" ->             MaxWeightSignCountyType.GERMAN_TYPE
    "GB" ->             MaxWeightSignCountyType.BRITISH_TYPE
    else ->             MaxWeightSignCountyType.GENERAL_TYPE
}

private fun getMaxWeightMgvSignLayoutResId(countryCode: String): Int = when (countryCode) {
    "AU", "CA", "US" -> R.layout.quest_maxweight_mgv_sign_us
    "FI", "IS", "SE" -> R.layout.quest_maxweight_mgv_sign_fi
    "DE" ->             R.layout.quest_maxweight_mgv_sign_de
    "GB" ->             R.layout.quest_maxweight_mgv_sign_gb
    else ->             R.layout.quest_maxweight_mgv_sign
}

private fun getMaxWeightAxleLoadSignType(countryCode: String): MaxWeightSignCountyType = when ( countryCode) {
    "AU", "CA", "US" -> MaxWeightSignCountyType.US_TYPE
    "FI", "IS", "SE" -> MaxWeightSignCountyType.FINISH_TYPE
    else ->             MaxWeightSignCountyType.GENERAL_TYPE
}
private fun getMaxWeightAxleLoadSignLayoutResId(countryCode: String): Int = when (countryCode) {
    "AU", "CA", "US" -> R.layout.quest_maxweight_axleload_sign_us
    "FI", "IS", "SE" -> R.layout.quest_maxweight_axleload_sign_fi
    else ->             R.layout.quest_maxweight_axleload_sign
}

private fun getMaxWeightTandemAxleLoadSignType(countryCode: String): MaxWeightSignCountyType = when ( countryCode) {
    "AU", "CA", "US" -> MaxWeightSignCountyType.US_TYPE
    "FI", "IS", "SE" -> MaxWeightSignCountyType.FINISH_TYPE
    else ->             MaxWeightSignCountyType.GENERAL_TYPE
}
private fun getMaxWeightTandemAxleLoadSignLayoutResId(countryCode: String): Int = when (countryCode) {
    "AU", "CA", "US" -> R.layout.quest_maxweight_tandem_axleload_sign_us
    "FI", "IS", "SE" -> R.layout.quest_maxweight_tandem_axleload_sign_fi
    else ->             R.layout.quest_maxweight_tandem_axleload_sign
}
