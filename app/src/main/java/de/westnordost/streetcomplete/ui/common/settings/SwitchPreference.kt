package de.westnordost.streetcomplete.ui.common.settings

import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.preferences.Preferences
import org.koin.compose.koinInject

@Composable
fun SwitchPreference(
    name: String,
    modifier: Modifier = Modifier,
    pref: String,
    default: Boolean,
    description: String? = null,
    onCheckedChange: (Boolean) -> Unit = { },
) {
    val prefs: Preferences = koinInject()
    var value by remember { mutableStateOf(prefs.getBoolean(pref, default)) }
    fun switched(newValue: Boolean) {
        value = newValue
        prefs.putBoolean(pref, newValue)
        onCheckedChange(newValue)
    }
    Preference(
        name = name,
        onClick = { switched(!value) },
        modifier = modifier,
        description = description
    ) {
        Switch(
            checked = value,
            onCheckedChange = { switched(it) }
        )
    }
}
