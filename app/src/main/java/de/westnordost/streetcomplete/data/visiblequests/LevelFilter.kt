package de.westnordost.streetcomplete.data.visiblequests

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.InputType
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.edit
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.quest.Quest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Controller for filtering all quests that are hidden because they are on the wrong level */
class LevelFilter internal constructor(private val sharedPrefs: SharedPreferences) : KoinComponent {
    var isEnabled = false
        private set
    var allowedLevel: String? = null
        private set
    lateinit var allowedLevelTags: List<String>
        private set

    private val mapDataSource: MapDataWithEditsSource by inject()
    private val visibleQuestTypeController: VisibleQuestTypeController by inject()
    private val selectedOverlaySource: SelectedOverlaySource by inject()

    init { reload() }

    private fun reload() {
        allowedLevel = sharedPrefs.getString(Prefs.ALLOWED_LEVEL, "").let { if (it.isNullOrBlank()) null else it.trim() }
        allowedLevelTags = sharedPrefs.getString(Prefs.ALLOWED_LEVEL_TAGS, "level,repeat_on,level:ref")!!.split(",")
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

    @SuppressLint("SetTextI18n") // tags should not be translated
    fun showLevelFilterDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.level_filter_title)
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        val levelTags = sharedPrefs.getString(Prefs.ALLOWED_LEVEL_TAGS, "level,repeat_on,level:ref")!!.split(",")

        val levelText = TextView(context)
        levelText.setText(R.string.level_filter_message)

        val level = EditText(context)
        level.inputType = InputType.TYPE_CLASS_TEXT
        level.setHint(R.string.level_filter_hint)
        level.setText(sharedPrefs.getString(Prefs.ALLOWED_LEVEL, ""))

        val enable = SwitchCompat(context)
        enable.setText(R.string.level_filter_enable)
        enable.isChecked = isEnabled

        val tagLevel = CheckBox(context)
        tagLevel.text = "level"
        tagLevel.isChecked = levelTags.contains("level")

        val tagRepeatOn = CheckBox(context)
        tagRepeatOn.text = "repeat_on"
        tagRepeatOn.isChecked = levelTags.contains("repeat_on")

        val tagLevelRef = CheckBox(context)
        tagLevelRef.text = "level:ref"
        tagLevelRef.isChecked = levelTags.contains("level:ref")

        val tagAddrFloor = CheckBox(context)
        tagAddrFloor.text = "addr:floor"
        tagAddrFloor.isChecked = levelTags.contains("addr:floor")

        linearLayout.addView(tagLevel)
        linearLayout.addView(tagRepeatOn)
        linearLayout.addView(tagLevelRef)
        linearLayout.addView(tagAddrFloor)
        linearLayout.addView(levelText)
        linearLayout.addView(level)
        linearLayout.addView(enable)
        linearLayout.setPadding(30,10,30,10)
        builder.setView(ScrollView(context).apply { addView(linearLayout) })
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val levelTagList = mutableListOf<String>()
            if (tagLevel.isChecked) levelTagList.add("level")
            if (tagRepeatOn.isChecked) levelTagList.add("repeat_on")
            if (tagLevelRef.isChecked) levelTagList.add("level:ref")
            if (tagAddrFloor.isChecked) levelTagList.add("addr:floor")
            sharedPrefs.edit {
                putString(Prefs.ALLOWED_LEVEL_TAGS, levelTagList.joinToString(","))
                putString(Prefs.ALLOWED_LEVEL, level.text.toString())
            }
            isEnabled = enable.isChecked
            reload()

            val overlayController = selectedOverlaySource as? SelectedOverlayController
            val tempOverlay = overlayController?.selectedOverlay
            if (tempOverlay != null) {
                // reload overlay (if enabled), also triggers quest reload
                overlayController.selectedOverlay = null
                overlayController.selectedOverlay = tempOverlay
            } else {
                visibleQuestTypeController.setVisibilities(emptyMap()) // trigger reload
            }
        }
        builder.show()
    }

}
