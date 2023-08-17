package de.westnordost.streetcomplete.screens

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import de.westnordost.streetcomplete.R

/** A Preference that can be activated to display a different background. */
class ActivatablePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.preferenceStyle,
    defStyleRes: Int = 0,
) : Preference(context, attrs, defStyleAttr, defStyleRes) {

    private var view: View? = null

    var activated = false
        set(value) {
            if (value != field) {
                view?.isActivated = value
            }
            field = value
        }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        view = holder.itemView
        holder.itemView.setBackgroundResource(R.drawable.background_activatable_selectable)
        if (activated) {
            holder.itemView.isActivated = true
        }
    }
}
