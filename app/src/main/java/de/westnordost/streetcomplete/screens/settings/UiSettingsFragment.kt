package de.westnordost.streetcomplete.screens.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.preference.DialogPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.util.dialogs.setViewWithDefaultPadding
import de.westnordost.streetcomplete.util.ktx.setUpToolbarTitleAndIcon
import org.koin.android.ext.android.inject

class UiSettingsFragment : PreferenceFragmentCompat(), HasTitle {

    private val prefs: ObservableSettings by inject()

    override val title: String get() = getString(R.string.pref_screen_ui)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.rootView.findViewById<Toolbar>(R.id.toolbar)?.apply {
            setUpToolbarTitleAndIcon(this)
        }
    }

    @SuppressLint("ResourceType") // for nearby quests... though it could probably be done in a nicer way
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences_ee_ui, false)
        addPreferencesFromResource(R.xml.preferences_ee_ui)

        findPreference<Preference>(Prefs.SHOW_NEARBY_QUESTS)?.setOnPreferenceClickListener {
            val builder = AlertDialog.Builder(requireContext())
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
                    prefs.putFloat(Prefs.SHOW_NEARBY_QUESTS_DISTANCE, it.coerceAtLeast(0.0f).coerceAtMost(10.0f))
                }
            }
            builder.show()
            true
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is DialogPreferenceCompat) {
            val fragment = preference.createDialog()
            fragment.arguments = bundleOf("key" to preference.key)
            fragment.setTargetFragment(this, 0)
            fragment.show(parentFragmentManager, "androidx.preference.PreferenceFragment.DIALOG")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

}
