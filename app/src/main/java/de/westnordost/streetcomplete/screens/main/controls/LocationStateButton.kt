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

    var state: State
        get() = _state ?: State.DENIED
        set(value) { _state = value }

    // this is necessary because state is accessed before it is initialized (in constructor of super)
    private var _state: State? = null
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

    private fun determineStateFrom(a: TypedArray): State = when {
        a.getBoolean(R.styleable.LocationStateButton_state_updating, false) ->  State.UPDATING
        a.getBoolean(R.styleable.LocationStateButton_state_searching, false) -> State.SEARCHING
        a.getBoolean(R.styleable.LocationStateButton_state_enabled, false) ->   State.ENABLED
        a.getBoolean(R.styleable.LocationStateButton_state_allowed, false) ->   State.ALLOWED
        else -> State.DENIED
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

    enum class State {
        DENIED, // user declined to give this app access to location
        ALLOWED, // user allowed this app to access location (but location disabled)
        ENABLED, // location service is turned on (but no location request active)
        SEARCHING, // requested location updates and waiting for first fix
        UPDATING;

        // receiving location updates
        val isEnabled: Boolean get() = ordinal >= ENABLED.ordinal
    }

    private val State.styleableAttributes: List<Int> get() =
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
