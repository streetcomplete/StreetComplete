package de.westnordost.streetcomplete.location

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Animatable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatImageButton
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.location.LocationState.*

/**
 * An image button which shows the current location state
 */
class LocationStateButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageButton(context, attrs, defStyle) {

    var state: LocationState
    get() = _state ?: DENIED
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
        a.getBoolean(R.styleable.LocationStateButton_state_updating,false) ->  UPDATING
        a.getBoolean(R.styleable.LocationStateButton_state_searching,false) -> SEARCHING
        a.getBoolean(R.styleable.LocationStateButton_state_enabled,false) ->   ENABLED
        a.getBoolean(R.styleable.LocationStateButton_state_allowed,false) ->   ALLOWED
        else -> DENIED
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

    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.state = state
        ss.activated = isActivated
        ss.navigation = isNavigation
        return ss
    }

    public override fun onRestoreInstanceState(s: Parcelable) {
        val ss = s as SavedState
        super.onRestoreInstanceState(ss.superState)
        state = ss.state
        isActivated = ss.activated
        isNavigation = ss.navigation
        requestLayout()
    }

    internal class SavedState : BaseSavedState {
        var state: LocationState = DENIED
        var activated = false
        var navigation = false

        constructor(superState: Parcelable?) : super(superState)
        constructor(parcel: Parcel) : super(parcel) {
            state = LocationState.valueOf(parcel.readString()!!)
            activated = parcel.readInt() == 1
            navigation = parcel.readInt() == 1
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(state.name)
            out.writeInt(if (activated) 1 else 0)
            out.writeInt(if (navigation) 1 else 0)
        }

        companion object {
            @JvmField @Keep
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
                override fun newArray(size: Int) = arrayOfNulls<SavedState>(size)
            }
        }
    }
}

private val LocationState.styleableAttributes: List<Int> get() =
    listOf(
        R.attr.state_allowed,
        R.attr.state_enabled,
        R.attr.state_searching,
        R.attr.state_updating
    ).subList(0, ordinal)
