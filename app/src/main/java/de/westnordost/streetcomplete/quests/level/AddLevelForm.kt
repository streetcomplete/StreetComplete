package de.westnordost.streetcomplete.quests.level

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import de.westnordost.streetcomplete.Injector

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.databinding.QuestLevelBinding
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.map.getPinIcon
import de.westnordost.streetcomplete.osm.SingleLevel
import de.westnordost.streetcomplete.osm.levelsIntersect
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.ShowsGeometryMarkers
import de.westnordost.streetcomplete.util.enclosingBoundingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddLevelForm : AbstractQuestFormAnswerFragment<String>() {

    @Inject internal lateinit var mapDataSource: MapDataWithEditsSource

    override val contentLayoutResId = R.layout.quest_level
    private val binding by contentViewBinding(QuestLevelBinding::bind)

    private lateinit var shopElementsAndGeometry: List<Pair<Element, ElementGeometry>>
    private var selectableLevelsById: MutableMap<Int, Double> = HashMap()
    private var selectedLevel: Double? = null
        set(value) {
            field = value
            checkIsFormComplete()
        }

    private val showsGeometryMarkersListener: ShowsGeometryMarkers? get() =
        parentFragment as? ShowsGeometryMarkers ?: activity as? ShowsGeometryMarkers

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleScope.launch { initializeButtons() }
    }

    private suspend fun initializeButtons() {
        val bbox = elementGeometry.center.enclosingBoundingBox(50.0)
        val mapData = withContext(Dispatchers.IO) { mapDataSource.getMapDataWithGeometry(bbox) }

        val shopsWithLevels = mapData.filter { it.tags["level"] != null && it.isSomeKindOfShop() }

        shopElementsAndGeometry = shopsWithLevels.mapNotNull { e ->
            mapData.getGeometry(e.type, e.id)?.let { geometry -> e to geometry }
        }
        val selectableLevels = shopsWithLevels.getSelectableLevels()

        val context = context ?: return
        val buttonSize = 64f.toPx(context).toInt()
        for (level in selectableLevels) {
            val id = View.generateViewId()
            val button = MaterialButton(requireContext(), null, R.attr.materialButtonOutlinedStyle)
            button.id = id
            button.layoutParams = LinearLayout.LayoutParams(buttonSize, buttonSize, 1f)
            button.text = level.toShortString()

            selectableLevelsById[id] = level
            binding.levelSelector.addView(button)
        }

        binding.levelSelector.addOnButtonCheckedListener { _, checkedId, isChecked ->
            selectedLevel = if (isChecked) selectableLevelsById[checkedId] else null
            updateMarkers(selectedLevel)
        }
    }

    private fun updateMarkers(level: Double?) {
        showsGeometryMarkersListener?.clearMarkersForCurrentQuest()
        if (level == null) return
        val levels = listOf(SingleLevel(level))
        for ((element, geometry) in shopElementsAndGeometry) {
            if (!element.getLevelsOrNull().levelsIntersect(levels)) continue
            val icon = getPinIcon(element.tags)
            val title = element.tags["name"] ?: element.tags["brand"]
            showsGeometryMarkersListener?.putMarkerForCurrentQuest(geometry, icon, title)
        }
    }

    override fun onClickOk() {
        applyAnswer(selectedLevel!!.toShortString())
    }

    override fun isFormComplete() = selectedLevel != null
}
