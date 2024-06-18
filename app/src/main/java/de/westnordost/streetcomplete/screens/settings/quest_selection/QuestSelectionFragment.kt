package de.westnordost.streetcomplete.screens.settings.quest_selection

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.compose.material.Surface
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.screens.settings.genericQuestTitle
import de.westnordost.streetcomplete.ui.util.composableContent
import de.westnordost.streetcomplete.util.ktx.containsAll
import de.westnordost.streetcomplete.util.ktx.observe
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/** Shows a screen in which the user can enable and disable quests as well as re-order them */
class QuestSelectionFragment : Fragment() {

    private val viewModel by viewModel<QuestSelectionViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        composableContent { Surface {
            QuestSelectionScreen(
                viewModel = viewModel,
                onClickBack = { parentFragmentManager.popBackStack() }
            )
        } }
/*
    private fun createOptionsMenu(toolbar: Toolbar) {
        toolbar.inflateMenu(R.menu.menu_quest_selection)

        val searchView = toolbar.menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                updateDisplayedQuestCount()
                return false
            }
        })

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_reset -> {
                    AlertDialog.Builder(requireContext())
                        .setMessage(R.string.pref_quests_reset)
                        .setPositiveButton(android.R.string.ok) { _, _ -> viewModel.resetQuestSelectionsAndOrder() }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                    true
                }
                R.id.action_deselect_all -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.pref_quests_deselect_all)
                        .setPositiveButton(android.R.string.ok) { _, _ -> viewModel.unselectAllQuests() }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                    true
                }
                else -> false
            }
        }
    }

 */
}
