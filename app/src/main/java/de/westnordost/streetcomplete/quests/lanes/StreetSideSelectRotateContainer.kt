package de.westnordost.streetcomplete.quests.lanes

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.children
import de.westnordost.streetcomplete.view.StreetRotateable
import kotlin.math.*

class StreetSideSelectRotateContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), StreetRotateable {

    private val view: View? get() = children.firstOrNull()

    init {
        addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
            val width = min(bottom - top, right - left)
            val height = max(bottom - top, right - left)
            val view = view
            if (view != null) {
                val params = view.layoutParams
                if (width != params.width || height != params.height) {
                    params.width = width
                    params.height = height
                    view.layoutParams = params
                }
            }
        }
    }

    override fun setStreetRotation(rotation: Float) {
        val view = view ?: return
        view.rotation = rotation
        val scale = abs(cos(rotation * PI / 180)).toFloat()
        view.scaleX = 1 + scale * 2 / 3f
        view.scaleY = 1 + scale * 2 / 3f
    }
}
