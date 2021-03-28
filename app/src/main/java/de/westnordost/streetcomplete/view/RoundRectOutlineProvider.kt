package de.westnordost.streetcomplete.view

import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.RequiresApi

/** Outline provider for a view thathas rounded courners. Used for casting the shadow of a view
 *  with rounded corners correctly. For example the speechbubble used for quest forms. */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class RoundRectOutlineProvider(
    private val radius: Float,
    private val marginLeft: Int = 0,
    private val marginTop: Int = 0,
    private val marginRight: Int = 0,
    private val marginBottom: Int = 0
) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        outline.setRoundRect(
            0 + marginLeft,
            0 + marginTop,
            view.width - marginRight,
            view.height - marginBottom,
            radius)
    }
}
