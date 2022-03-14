package de.westnordost.streetcomplete.screens.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.R

abstract class DialogPreferenceCompat @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.dialogPreferenceStyle,
    defStyleRes: Int = 0
) : DialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    abstract fun createDialog(): PreferenceDialogFragmentCompat
}
