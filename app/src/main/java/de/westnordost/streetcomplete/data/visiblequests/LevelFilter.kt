package de.westnordost.streetcomplete.data.visiblequests

import android.content.Context
import android.view.LayoutInflater
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.databinding.DialogLevelFilterBinding
import de.westnordost.streetcomplete.osm.level.LevelTypes
import de.westnordost.streetcomplete.osm.level.parseSelectableLevels
import de.westnordost.streetcomplete.screens.main.map.MapFragment
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.ceil
import kotlin.math.floor

/** Controller for filtering all quests that are hidden because they are on the wrong level */
class LevelFilter internal constructor(private val prefs: ObservableSettings) : KoinComponent {
    var isEnabled = false
        private set
    var allowedLevel: String? = null
        private set
    lateinit var allowedLevelTags: Set<String>
        private set

    private val mapDataSource: MapDataWithEditsSource by inject()
    private val visibleQuestTypeController: VisibleQuestTypeController by inject()
    private val visibleQuestsSource: VisibleQuestsSource by inject()
    private val selectedOverlaySource: SelectedOverlaySource by inject()

    init { reload() }

    private fun reload() {
        allowedLevel = prefs.getString(Prefs.ALLOWED_LEVEL, "").let { if (it.isBlank()) null else it.trim() }
        allowedLevelTags = prefs.getString(Prefs.ALLOWED_LEVEL_TAGS, "level,repeat_on,level:ref").split(",").toHashSet()
    }

    fun isVisible(quest: Quest): Boolean =
        !isEnabled || when (quest) {
            is OsmQuest -> levelAllowed(mapDataSource.get(quest.elementType, quest.elementId))
            is ExternalSourceQuest -> levelAllowed(quest.elementKey?.let { mapDataSource.get(it.type, it.id) })
            else -> true
        }

    fun levelAllowed(element: Element?): Boolean {
        if (!isEnabled) return true
        val tags = element?.tags ?: return true
        val levelTags = tags.filterKeys { allowedLevelTags.contains(it) }
        if (levelTags.isEmpty()) return allowedLevel == null
        val allowedLevel = allowedLevel ?: return false
        levelTags.values.forEach { value ->
            val levels = value.split(";")
            if (allowedLevel == "*") return true // we have anything in an allowed tag, that's enough
            if (allowedLevel.startsWith('<')) {
                val maxLevel = allowedLevel.substring(1).trim().toFloatOrNull()
                if (maxLevel != null)
                    levels.forEach { level ->
                        level.toFloatOrNull()?.let { if (it < maxLevel) return true }
                    }
            }
            if (allowedLevel.startsWith('>')) {
                val minLevel = allowedLevel.substring(1).trim().toFloatOrNull()
                if (minLevel != null)
                    levels.forEach { level ->
                        level.toFloatOrNull()?.let { if (it > minLevel) return true }
                    }
            }
            if (levels.contains(allowedLevel)) return true
            if (value == allowedLevel) return true // maybe user entered 0;1
        }
        return false
    }

    fun showLevelFilterDialog(context: Context, mapFragment: MapFragment?) {
        val builder = AlertDialog.Builder(context)
        val binding = DialogLevelFilterBinding.inflate(LayoutInflater.from(context))
        builder.setTitle(R.string.level_filter_title)
        binding.level.setText(prefs.getString(Prefs.ALLOWED_LEVEL, ""))
        binding.enableSwitch.isChecked = isEnabled
        val levelTags = prefs.getString(Prefs.ALLOWED_LEVEL_TAGS, "level,repeat_on,level:ref").split(",")
        val allowedLevelTypes = LevelTypes.entries.filter { levelTags.contains(it.tag) }
        binding.plus.setOnClickListener {
            val selectableLevels = getLevelsInView(mapFragment?.getDisplayedArea(), allowedLevelTypes)
            val oldText = binding.level.text?.toString()
            val currentLevel = oldText?.let { "[\\d.+-]+".toRegex().find(it)?.value }
            val currentLevelNumber = currentLevel?.toDoubleOrNull()
            val newLevel = if (currentLevelNumber == null) {
                selectableLevels.find { it >= 0 } ?: selectableLevels.firstOrNull() ?: 0.0
            } else {
                val nextInt = floor(currentLevelNumber + 1.0)
                selectableLevels.find { it > currentLevelNumber && it < nextInt } ?: nextInt
            }
            binding.level.setText(oldText?.replace(currentLevel ?: oldText, newLevel.toNiceString()) ?: newLevel.toNiceString())
        }
        binding.minus.setOnClickListener {
            val selectableLevels = getLevelsInView(mapFragment?.getDisplayedArea(), allowedLevelTypes)
            val oldText = binding.level.text?.toString()
            val currentLevel = oldText?.let { "[\\d.+-]+".toRegex().find(it)?.value }
            val currentLevelNumber = currentLevel?.toDoubleOrNull()
            val newLevel = if (currentLevelNumber == null) {
                selectableLevels.findLast { it <= 0 } ?: selectableLevels.firstOrNull() ?: 0.0
            } else {
                val prevInt = ceil(currentLevelNumber - 1.0)
                selectableLevels.findLast { it < currentLevelNumber && it > prevInt } ?: prevInt
            }
            binding.level.setText(oldText?.replace(currentLevel ?: oldText, newLevel.toNiceString()) ?: newLevel.toNiceString())
        }

        binding.levelBox.isChecked = allowedLevelTypes.contains(LevelTypes.LEVEL)
        binding.repeatOnBox.isChecked = allowedLevelTypes.contains(LevelTypes.REPEAT_ON)
        binding.levelRefBox.isChecked = allowedLevelTypes.contains(LevelTypes.LEVEL_REF)
        binding.addrFloorBox.isChecked = allowedLevelTypes.contains(LevelTypes.ADDR_FLOOR)

        builder.setView(ScrollView(context).apply { addView(binding.root) })
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val levelTagList = mutableListOf<String>()
            if (binding.levelBox.isChecked) levelTagList.add("level")
            if ( binding.repeatOnBox.isChecked) levelTagList.add("repeat_on")
            if (binding.levelRefBox.isChecked) levelTagList.add("level:ref")
            if (binding.addrFloorBox.isChecked) levelTagList.add("addr:floor")
            prefs.putString(Prefs.ALLOWED_LEVEL_TAGS, levelTagList.joinToString(","))
            prefs.putString(Prefs.ALLOWED_LEVEL, binding.level.text.toString())
            isEnabled = binding.enableSwitch.isChecked
            reload()

            val overlayController = selectedOverlaySource as? SelectedOverlayController
            val tempOverlay = overlayController?.selectedOverlay
            if (tempOverlay != null) {
                // reload overlay (if enabled), also triggers quest reload unless HIDE_OVERLAY_QUESTS disabled
                overlayController.selectedOverlay = null
                overlayController.selectedOverlay = tempOverlay
                if (!prefs.getBoolean(Prefs.HIDE_OVERLAY_QUESTS, true))
                    visibleQuestTypeController.setVisibilities(emptyMap()) // trigger reload
            } else {
                visibleQuestTypeController.setVisibilities(emptyMap()) // trigger reload
            }
        }
        builder.show()
    }

    private fun getLevelsInView(displayedArea: BoundingBox?, allowed: List<LevelTypes>): List<Double> {
        val tags = if (displayedArea != null) {
            visibleQuestsSource.getAllVisible(displayedArea).mapNotNull {
                when (it) {
                    is OsmQuest -> mapDataSource.get(it.elementType, it.elementId)
                    is ExternalSourceQuest -> it.elementKey?.let { mapDataSource.get(it.type, it.id) }
                    else -> null
                }?.tags
            }
        } else emptyList()
        return parseSelectableLevels(tags, allowed)
    }

    private fun Double.toNiceString(): String {
        if (toInt().toDouble() == this) return toInt().toString()
        return toString()
    }

}
