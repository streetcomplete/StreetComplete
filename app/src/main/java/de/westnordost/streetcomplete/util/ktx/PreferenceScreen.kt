package de.westnordost.streetcomplete.util.ktx

import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import androidx.preference.forEach

fun PreferenceGroup.forEachRecursive(action: (Preference) -> Unit) {
    forEach { preference ->
        if (preference is PreferenceGroup) {
            preference.forEachRecursive(action)
        } else {
            action(preference)
        }
    }
}
