package de.westnordost.streetcomplete.screens

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.FragmentOnAttachListener
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.asRecursiveSequence

/** A two pane list fragment that handles showing a divider to separate the detail view and allows
 * highlighting the preference belonging to the fragment shown in the detail pane. */
abstract class TwoPaneListFragment : PreferenceFragmentCompat(),
    TwoPaneHeaderFragment.PaneListener {

    private var singlePane = false
    private var divider: View? = null
    private var detailPanePreference: ActivatablePreference? = null
        set(value) {
            if (value != field && value != null) {
                field?.activated = false
                if (!singlePane) {
                    value.activated = true
                }
            }
            field = value
        }

    private val onAttachFragmentListener = FragmentOnAttachListener { _, fragment ->
        // Highlight initial selection made by onCreateInitialDetailFragment
        for (p in preferenceScreen.asRecursiveSequence()) {
            if (p is ActivatablePreference && p.fragment == fragment.javaClass.name) {
                detailPanePreference = p
                break
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        divider = view.findViewById(R.id.divider)
        savedInstanceState?.getString(DETAIL_PANE_PREFERENCE)?.let {
            detailPanePreference = findPreference(it)
        }
        parentFragmentManager.addFragmentOnAttachListener(onAttachFragmentListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        parentFragmentManager.removeFragmentOnAttachListener(onAttachFragmentListener)
    }

    override fun onPanesChanged(singlePane: Boolean) {
        this.singlePane = singlePane
        divider?.isGone = singlePane
        detailPanePreference?.activated = !singlePane
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(DETAIL_PANE_PREFERENCE, detailPanePreference?.key)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (preference is ActivatablePreference) {
            detailPanePreference = preference
        }
        return super.onPreferenceTreeClick(preference)
    }

    companion object {

        private const val DETAIL_PANE_PREFERENCE = "detail_pane_preference"
    }
}
