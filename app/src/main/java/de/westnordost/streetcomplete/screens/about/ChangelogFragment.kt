package de.westnordost.streetcomplete.screens.about

import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentChangelogBinding
import de.westnordost.streetcomplete.databinding.RowChangelogBinding
import de.westnordost.streetcomplete.util.ktx.getYamlStringMap
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.ListAdapter
import de.westnordost.streetcomplete.view.setHtml
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Shows the full changelog */
class ChangelogFragment : Fragment(R.layout.fragment_changelog) {

    private val binding by viewBinding(FragmentChangelogBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.changelogList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        viewLifecycleScope.launch {
            val changelog = readChangelog(resources)
            binding.changelogList.adapter = ChangelogAdapter(changelog)
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.setTitle(R.string.about_title_changelog)
    }
}

/** A dialog that shows the changelog */
class WhatsNewDialog(context: Context, sinceVersion: String) : AlertDialog(context) {

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        val binding = FragmentChangelogBinding.inflate(LayoutInflater.from(context))
        binding.changelogList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        setTitle(R.string.title_whats_new)
        setView(binding.root)
        setButton(DialogInterface.BUTTON_POSITIVE, context.resources.getText(android.R.string.ok), null, null)

        scope.launch {
            val fullChangelog = readChangelog(context.resources)
            var currentVersionIndex = fullChangelog.indexOfFirst { it.title == sinceVersion }
            // if version not found, just show the last one
            if (currentVersionIndex == -1) currentVersionIndex = 1
            val changelog = fullChangelog.subList(0, currentVersionIndex)

            binding.changelogList.adapter = ChangelogAdapter(changelog)
        }
    }

    override fun dismiss() {
        super.dismiss()
        scope.cancel()
    }
}

class ChangelogAdapter(changelog: List<Release>) : ListAdapter<Release>(changelog) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(RowChangelogBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    inner class ViewHolder(val binding: RowChangelogBinding) : ListAdapter.ViewHolder<Release>(binding) {
        override fun onBind(with: Release) {
            binding.titleLabel.text = with.title
            binding.descriptionLabel.setHtml(with.description)
        }
    }
}

data class Release(val title: String, val description: String)

private suspend fun readChangelog(resources: Resources): List<Release> = withContext(Dispatchers.IO) {
    resources.getYamlStringMap(R.raw.changelog).map { Release(it.key, addedLinks(it.value)) }
}

private fun addedLinks(description: String): String {
    return description
        .replace(Regex("(?<=[\\s(]|^)#(\\d+)")) { matchResult ->
            val issue = matchResult.groupValues[1]
            "<a href=\"https://github.com/streetcomplete/StreetComplete/issues/$issue\">#$issue</a>"
        }
        .replace(Regex("(?<=[\\s(]|^)@([a-zA-Z\\d-]+)")) { matchResult ->
            val contributor = matchResult.groupValues[1]
            "<a href=\"https://github.com/$contributor\">$contributor</a>"
        }
}
