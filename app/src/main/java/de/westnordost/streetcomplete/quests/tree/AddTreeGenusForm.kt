package de.westnordost.streetcomplete.quests.tree

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.widget.doAfterTextChanged
import de.westnordost.osmfeatures.StringUtils
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.databinding.QuestNameSuggestionBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.screens.main.map.getTreeGenus
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.SearchAdapter
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.mostCommonWithin
import org.koin.android.ext.android.inject
import java.io.File
import java.io.IOException

class AddTreeGenusForm : AbstractOsmQuestForm<Tree>() {

    override val contentLayoutResId = R.layout.quest_name_suggestion
    private val binding by contentViewBinding(QuestNameSuggestionBinding::bind)
    private val name: String get() = binding.nameInput.text?.toString().orEmpty().trim()
    private val mapDataSource: MapDataWithEditsSource by inject()
    private val trees get() = loadTrees()

    override fun onClickOk() {
        val tree = getSelectedTree()
        if (tree == null) {
            binding.nameInput.error = context?.resources?.getText(R.string.quest_tree_error)
        } else {
            favs.add("${tree.isSpecies}§${tree.name}")
            applyAnswer(tree)
        }
    }

    override fun isFormComplete(): Boolean {
        return name.isNotEmpty()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SearchAdapter(requireContext(), { getTrees(it) }, { it.toDisplayString() })
        binding.nameInput.setAdapter(adapter)
        binding.nameInput.doAfterTextChanged { checkIsFormComplete() }
        // set some blank input
        // this is the only way I found how to display recent answers
        binding.nameInput.setOnFocusChangeListener { _, focused ->
            if (focused) binding.nameInput.setText(" ", true)
        }
        binding.nameInput.doOnLayout { binding.nameInput.dropDownWidth = binding.nameInput.width - requireContext().dpToPx(60).toInt() }
        binding.nameInput.requestFocus()
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        val maxDist = clickAreaSizeInMeters + 5
        val bbox = position.enclosingBoundingBox(maxDist)
        val mapData = mapDataSource.getMapDataWithGeometry(bbox)
        var bestTree: Pair<String, Double>? = null

        mapData.forEach { element ->
            if (element is Node && element.tags["natural"] == "tree") {
                val name = getTreeGenus(element.tags) ?: return@forEach
                val distance = element.position.distanceTo(position)
                if (distance < (bestTree?.second ?: maxDist))
                    bestTree = Pair(name, distance)
            }
        }
        bestTree?.let { binding.nameInput.setText(getTrees(it.first).firstOrNull()?.toDisplayString() ?: "not found", false) }

        return true
    }

    private fun getSelectedTree(): Tree? {
        val input = binding.nameInput.text.toString()
        return getTrees(input).firstOrNull { StringUtils.canonicalize(it.toDisplayString()) == StringUtils.canonicalize(input) }
    }

    private fun getTrees(fullSearch: String): List<Tree> {
        val search = fullSearch.trim()
        // not working, i need a tree with the same name and species, but local name?
        if (search.isEmpty()) return lastPickedAnswers.mapNotNull { answer ->
            val treeString = answer.split('§')
            trees.firstOrNull { it.name == treeString[1] && it.isSpecies == (treeString[0] == "true") }
        }
        return trees.filter { tree ->
            tree.toDisplayString() == search
            || tree.name == search
            || tree.name.split(" ").any { it.startsWith(search, true) }
            || tree.localName?.contains(search, true) == true
        //sorting: genus-only first, then prefer trees with localName
        }.sortedBy { it.localName == null }.sortedBy { it.isSpecies }
    }

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            prefs,
            key = javaClass.simpleName,
            serialize = { it },
            deserialize = { it },
            maxEntries = 25
        )
    }

    private lateinit var favs: LastPickedValuesStore<String>

    private val lastPickedAnswers by lazy {
        favs.get()
            .mostCommonWithin(target = 3, historyCount = 25, first = 1)
            .toList()
    }

    private fun loadTrees(): Set<Tree> {
        if (treeSet.isNotEmpty()) return treeSet
        val c = context ?: return emptySet()
        // load from file, assuming format: <Genus/Species> (<localName>)
        //  assume species if it contains a space character
        try {
            c.getExternalFilesDir(null)?.let { dir ->
                treeSet.addAll(File(dir, FILENAME_TREES).readLines().mapNotNull { it.toTree(it.substringBefore(" (").contains(" ")) })
            }
        } catch (_: IOException) { } // file may not exist, so an exception is no surprise

        try {
            c.assets.open("tree/otherDataGenus.txt").bufferedReader().lineSequence().mapNotNullTo(treeSet) { it.toTree(false) }
            c.assets.open("tree/otherDataSpecies.txt").bufferedReader().lineSequence().mapNotNullTo(treeSet) { it.toTree(true) }
            c.assets.open("tree/osmGenus.txt").bufferedReader().lineSequence().mapNotNullTo(treeSet) { it.toTree(false) }
            c.assets.open("tree/osmSpecies.txt").bufferedReader().lineSequence().mapNotNullTo(treeSet) { it.toTree(true) }
        } catch (_: IOException) { }
        return treeSet
    }

    companion object {
        private val treeSet = mutableSetOf<Tree>()
    }

}

private fun String.toTree(isSpecies: Boolean): Tree? {
    val line = trim()
    if (line.isBlank()) return null
    val localName = if (line.contains(" (") && line.contains(')'))
        line.substringAfter("(").substringBeforeLast(")")
    else null
    return Tree(line.substringBefore(" (").intern(), isSpecies, localName)
}

const val FILENAME_TREES = "trees.csv"
