package de.westnordost.streetcomplete.screens.main.controls

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.os.bundleOf
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.parcelable
import de.westnordost.streetcomplete.util.ktx.serializable

/**
 * An image button which shows the current location state
 */
class LocationStateButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageButton(context, attrs, defStyle) {

    var state: LocationState
        get() = _state ?: LocationState.DENIED
        set(value) { _state = value }

    // this is necessary because state is accessed before it is initialized (in constructor of super)
    private var _state: LocationState? = null
        set(value) {
            if (field != value) {
                field = value
                refreshDrawableState()
            }
        }

    private val tint: ColorStateList?

    var isNavigation: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                refreshDrawableState()
            }
        }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LocationStateButton)
        state = determineStateFrom(a)
        tint = a.getColorStateList(R.styleable.LocationStateButton_tint)
        isNavigation = a.getBoolean(R.styleable.LocationStateButton_is_navigation, false)

        a.recycle()
    }

    private fun determineStateFrom(a: TypedArray): LocationState = when {
        a.getBoolean(R.styleable.LocationStateButton_state_updating, false) ->  LocationState.UPDATING
        a.getBoolean(R.styleable.LocationStateButton_state_searching, false) -> LocationState.SEARCHING
        a.getBoolean(R.styleable.LocationStateButton_state_enabled, false) ->   LocationState.ENABLED
        a.getBoolean(R.styleable.LocationStateButton_state_allowed, false) ->   LocationState.ALLOWED
        else -> LocationState.DENIED
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        // autostart
        val current = drawable.current
        if (current is Animatable) {
            if (!current.isRunning) current.start()
        }
        if (tint != null && tint.isStateful) {
            setColorFilter(tint.getColorForState(drawableState, 0))
        }
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val attributes = ArrayList<Int>()
        attributes += state.styleableAttributes
        if (isNavigation) attributes += R.attr.is_navigation

        val drawableState = super.onCreateDrawableState(extraSpace + attributes.size)

        View.mergeDrawableStates(drawableState, attributes.toIntArray())
        return drawableState
    }

    override fun onSaveInstanceState() = bundleOf(
        KEY_SUPER_STATE to super.onSaveInstanceState(),
        KEY_STATE to state,
        KEY_IS_ACTIVATED to isActivated,
        KEY_IS_NAVIGATION to isNavigation,
    )

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.parcelable(KEY_SUPER_STATE))
            this.state = state.serializable(KEY_STATE)!!
            isActivated = state.getBoolean(KEY_IS_ACTIVATED)
            isNavigation = state.getBoolean(KEY_IS_NAVIGATION)
            requestLayout()
        }
    }

    private val LocationState.styleableAttributes: List<Int> get() =
        listOf(
            R.attr.state_allowed,
            R.attr.state_enabled,
            R.attr.state_searching,
            R.attr.state_updating
        ).subList(0, ordinal)

    companion object {
        private const val KEY_SUPER_STATE = "superState"
        private const val KEY_STATE = "state"
        private const val KEY_IS_ACTIVATED = "isActivated"
        private const val KEY_IS_NAVIGATION = "isNavigation"
    }
}
