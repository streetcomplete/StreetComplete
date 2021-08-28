package de.westnordost.streetcomplete.view

import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.RequiresApi

/** Outline provider for a view that should appear oval. Used for casting the shadow of an oval-
 *  shaped view correctly */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
object CircularOutlineProvider : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        outline.setOval(0,0,view.width,view.height)
    }
}
