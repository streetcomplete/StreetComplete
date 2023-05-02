package de.westnordost.streetcomplete.util.ktx

import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import androidx.preference.forEach

fun PreferenceGroup.asRecursiveSequence(): Sequence<Preference> = sequence {
    forEach { preference ->
        if (preference is PreferenceGroup) {
            yieldAll(preference.asRecursiveSequence())
        } else {
            yield(preference)
        }
    }
}
