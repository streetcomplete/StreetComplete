package de.westnordost.streetcomplete.settings

import android.content.Context
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import android.util.AttributeSet
import androidx.preference.R

abstract class DialogPreferenceCompat @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.dialogPreferenceStyle,
    defStyleRes: Int = 0): DialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    abstract fun createDialog(): PreferenceDialogFragmentCompat
}
