package de.westnordost.streetcomplete.quests.level

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.databinding.QuestLevelBinding
import de.westnordost.streetcomplete.ktx.getLevelsOrNull
import de.westnordost.streetcomplete.ktx.getSelectableLevels
import de.westnordost.streetcomplete.ktx.toShortString
import de.westnordost.streetcomplete.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.map.getPinIcon
import de.westnordost.streetcomplete.map.getTitle
import de.westnordost.streetcomplete.osm.SingleLevel
import de.westnordost.streetcomplete.osm.levelsIntersect
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.ShowsGeometryMarkers
import de.westnordost.streetcomplete.util.TextChangedWatcher
import de.westnordost.streetcomplete.util.enclosingBoundingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import kotlin.math.ceil
import kotlin.math.floor

class AddLevelForm : AbstractQuestFormAnswerFragment<String>() {

    private val mapDataSource: MapDataWithEditsSource by inject()

    override val contentLayoutResId = R.layout.quest_level
    private val binding by contentViewBinding(QuestLevelBinding::bind)

    private lateinit var shopElementsAndGeometry: List<Pair<Element, ElementGeometry>>

    private val showsGeometryMarkersListener: ShowsGeometryMarkers? get() =
        parentFragment as? ShowsGeometryMarkers ?: activity as? ShowsGeometryMarkers

    private var selectedLevel: Double?
        get() = binding.levelInput.text.toString().trim().toDoubleOrNull()
        set(value) { binding.levelInput.setText(value?.toShortString() ?: "") }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.levelInput.addTextChangedListener(TextChangedWatcher { onSelectedLevel() })

        viewLifecycleScope.launch { initializeButtons() }
    }

    private suspend fun initializeButtons() {
        val bbox = elementGeometry.center.enclosingBoundingBox(50.0)
        val mapData = withContext(Dispatchers.IO) { mapDataSource.getMapDataWithGeometry(bbox) }

        val shopsWithLevels = mapData.filter {
            it.tags["level"] != null && IS_SHOP_OR_DISUSED_SHOP_EXPRESSION.matches(it)
        }

        shopElementsAndGeometry = shopsWithLevels.mapNotNull { e ->
            mapData.getGeometry(e.type, e.id)?.let { geometry -> e to geometry }
        }
        if (selectedLevel != null) {
            updateMarkers(selectedLevel)
        }

        val selectableLevels = shopsWithLevels.getSelectableLevels()
        binding.plusMinusContainer.addButton.setOnClickListener {
            val level = selectedLevel
            selectedLevel = if (level != null) {
                /* usually +1, but if the selectable levels contain any intermediate floors
                   (e.g. 0.5), step to these instead */
                val nextInt = floor(level + 1.0)
                selectableLevels.find { it > level && it < nextInt } ?: nextInt
            } else {
                selectableLevels.find { it >= 0 } ?: selectableLevels.firstOrNull() ?: 0.0
            }
        }

        binding.plusMinusContainer.subtractButton.setOnClickListener {
            val level = selectedLevel
            selectedLevel = if (level != null) {
                val prevInt = ceil(level - 1.0)
                selectableLevels.findLast { it < level && it > prevInt } ?: prevInt
            } else {
                selectableLevels.findLast { it <= 0 } ?: selectableLevels.firstOrNull() ?: 0.0
            }
        }
    }

    private fun onSelectedLevel() {
        checkIsFormComplete()
        updateMarkers(selectedLevel)
    }

    private fun updateMarkers(level: Double?) {
        showsGeometryMarkersListener?.clearMarkersForCurrentQuest()
        if (level == null) return
        val levels = listOf(SingleLevel(level))
        for ((element, geometry) in shopElementsAndGeometry) {
            if (!element.getLevelsOrNull().levelsIntersect(levels)) continue
            val icon = getPinIcon(element.tags)
            val title = getTitle(element.tags)
            showsGeometryMarkersListener?.putMarkerForCurrentQuest(geometry, icon, title)
        }
    }

    override fun onClickOk() {
        applyAnswer(selectedLevel!!.toShortString())
    }

    override fun isFormComplete() = selectedLevel != null
}
