package de.westnordost.streetcomplete.screens.settings

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.dialogs.TextInputDialog
import de.westnordost.streetcomplete.ui.common.settings.Preference
import de.westnordost.streetcomplete.ui.common.settings.SwitchPreference
import de.westnordost.streetcomplete.util.dialogs.setViewWithDefaultPadding
import org.koin.compose.koinInject

@Composable
fun UiSettingsScreen(
    onClickBack: () -> Unit,
) {
    val ctx = LocalContext.current
    val prefs: Preferences = koinInject()
    var showMinLinesDialog by remember { mutableStateOf(false) }
    var showRotateAngleDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.pref_screen_ui)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                    )
                )
        ) {
            SwitchPreference(
                name = stringResource(R.string.pref_show_quick_settings_title),
                pref = Prefs.QUICK_SETTINGS,
                default = false,
            )
            SwitchPreference(
                name = stringResource(R.string.pref_overlay_quick_selector_title),
                pref = Prefs.OVERLAY_QUICK_SELECTOR,
                default = false,
            )
            SwitchPreference(
                name = stringResource(R.string.pref_show_next_quest_title),
                description = stringResource(R.string.pref_show_next_quest_summary),
                pref = Prefs.SHOW_NEXT_QUEST_IMMEDIATELY,
                default = false,
            )
            Preference(
                name = stringResource(R.string.pref_show_nearby_quests_title),
                onClick = { nearbyQuestDialog(prefs, ctx) },
                description = stringResource(R.string.pref_show_nearby_quests_summary)
            )
            SwitchPreference(
                name = stringResource(R.string.pref_hide_button_title),
                description = stringResource(R.string.pref_hide_button_summary),
                pref = Prefs.SHOW_HIDE_BUTTON,
                default = false,
            )
            SwitchPreference(
                name = stringResource(R.string.pref_create_node_show_keyboard_title),
                pref = Prefs.CREATE_NODE_SHOW_KEYBOARD,
                default = true,
            )
            SwitchPreference(
                name = stringResource(R.string.pref_select_first_edit_title),
                description = stringResource(R.string.pref_select_first_edit_summary),
                pref = Prefs.SELECT_FIRST_EDIT,
                default = true,
            )
            SwitchPreference(
                name = stringResource(R.string.pref_search_more_languages_title),
                description = stringResource(R.string.pref_search_more_languages_summary),
                pref = Prefs.SEARCH_MORE_LANGUAGES,
                default = false,
            )
            Preference(
                name = stringResource(R.string.pref_recent_answers_first_min_lines),
                onClick = { showMinLinesDialog = true },
                description = stringResource(R.string.pref_recent_answers_first_min_lines_summary, prefs.getInt(Prefs.FAVS_FIRST_MIN_LINES, 1))
            )
            SwitchPreference(
                name = stringResource(R.string.pref_disable_navigation_mode_title),
                description = stringResource(R.string.pref_disable_navigation_mode_summary),
                pref = Prefs.DISABLE_NAVIGATION_MODE,
                default = false,
            )
            SwitchPreference(
                name = stringResource(R.string.pref_main_menu_grid),
                pref = Prefs.MAIN_MENU_FULL_GRID,
                default = false,
            )
            SwitchPreference(
                name = stringResource(R.string.pref_main_menu_switch_presets_title),
                pref = Prefs.MAIN_MENU_SWITCH_PRESETS,
                default = false,
            )
            SwitchPreference(
                name = stringResource(R.string.pref_caps_word_name_input),
                pref = Prefs.CAPS_WORD_NAME_INPUT,
                default = false,
            )
            SwitchPreference(
                name = stringResource(R.string.pref_volume_zoom_title),
                description = stringResource(R.string.pref_volume_zoom_summary),
                pref = Prefs.VOLUME_ZOOM,
                default = false,
            )
            SwitchPreference(
                name = stringResource(R.string.pref_rotate_while_zooming_title),
                pref = Prefs.ROTATE_WHILE_ZOOMING,
                default = false,
            )
            Preference(
                name = stringResource(R.string.pref_rotate_angle_threshold_title),
                onClick = { showRotateAngleDialog = true },
            )
        }
    }
    if (showMinLinesDialog)
        TextInputDialog(
            onDismissRequest = { showMinLinesDialog = false },
            onConfirmed = { prefs.putInt(Prefs.FAVS_FIRST_MIN_LINES, it.toIntOrNull() ?: 1) },
            text = prefs.getInt(Prefs.FAVS_FIRST_MIN_LINES, 1).toString(),
            //title = { Text(stringResource(R.string.pref_recent_answers_first_min_lines)) },
            title = { Text(stringResource(R.string.pref_recent_answers_first_min_lines_message)) },
            keyboardType = KeyboardType.Number,
            checkTextValid = {
                val value = it.toIntOrNull()
                value != null && value >= 0
            }
        )
    if (showRotateAngleDialog)
        TextInputDialog(
            onDismissRequest = { showRotateAngleDialog = false },
            onConfirmed = { prefs.prefs.putFloat(Prefs.ROTATE_ANGLE_THRESHOLD, it.toFloatOrNull() ?: 1.5f) },
            text = prefs.getFloat(Prefs.ROTATE_ANGLE_THRESHOLD, 1.5f).toString(),
            title = { Text(stringResource(R.string.pref_rotate_angle_threshold_title)) },
            //textInputLabel = { Text(stringResource(R.string.pref_search_more_languages_summary)) },
            keyboardType = KeyboardType.Decimal,
            checkTextValid = {
                val value = it.toFloatOrNull()
                value != null && value >= 0
            }
        )
}

// todo: composable
@SuppressLint("ResourceType") // for nearby quests... though it could probably be done in a nicer way
private fun nearbyQuestDialog(prefs: Preferences, context: Context) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle(R.string.pref_show_nearby_quests_title)
    val linearLayout = LinearLayout(context)
    linearLayout.orientation = LinearLayout.VERTICAL

    val buttons = RadioGroup(context)
    buttons.orientation = RadioGroup.VERTICAL
    buttons.addView(RadioButton(context).apply {
        setText(R.string.show_nearby_quests_disable)
        id = 0
    })
    buttons.addView(RadioButton(context).apply {
        setText(R.string.show_nearby_quests_visible)
        id = 1
    })
    buttons.addView(RadioButton(context).apply {
        setText(R.string.show_nearby_quests_all_types)
        id = 2
        if (!prefs.getBoolean(Prefs.EXPERT_MODE, false)) isEnabled = false
    })
    buttons.addView(RadioButton(context).apply {
        setText(R.string.show_nearby_quests_even_hidden)
        id = 3
        if (!prefs.getBoolean(Prefs.EXPERT_MODE, false)) isEnabled = false
    })
    buttons.check(prefs.getInt(Prefs.SHOW_NEARBY_QUESTS, 0))
    buttons.setOnCheckedChangeListener { _, _ ->
        if (buttons.checkedRadioButtonId in 0..3)
            prefs.putInt(Prefs.SHOW_NEARBY_QUESTS, buttons.checkedRadioButtonId)
    }

    val distanceText = TextView(context).apply { setText(R.string.show_nearby_quests_distance) }

    val distance = EditText(context).apply {
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        setText(prefs.getFloat(Prefs.SHOW_NEARBY_QUESTS_DISTANCE, 0.0f).toString())
    }
    linearLayout.addView(buttons)
    linearLayout.addView(distanceText)
    linearLayout.addView(distance)

    builder.setViewWithDefaultPadding(linearLayout)
    builder.setPositiveButton(android.R.string.ok) { _, _ ->
        distance.text.toString().toFloatOrNull()?.let {
            prefs.prefs.putFloat(Prefs.SHOW_NEARBY_QUESTS_DISTANCE, it.coerceAtLeast(0.0f).coerceAtMost(10.0f))
        }
    }
    builder.show()
}
