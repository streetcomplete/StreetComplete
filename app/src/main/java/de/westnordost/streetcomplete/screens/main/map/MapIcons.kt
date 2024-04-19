package de.westnordost.streetcomplete.screens.main.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import androidx.core.graphics.toRect
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.AllEditTypes
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.iconResId
import de.westnordost.streetcomplete.osm.building.iconResName
import de.westnordost.streetcomplete.util.ktx.createBitmap
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.toSdf
import de.westnordost.streetcomplete.view.presetIconIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.math.ceil

class MapIcons(
    private val context: Context,
    private val allEditTypes: AllEditTypes,
) {
    val pinBitmaps by lazy { createPinBitmaps() }
    val presetBitmaps by lazy { createPresetBitmaps() }
    val markerBitmaps by lazy { createMarkerBitmaps() }

    private fun createPinBitmaps(): HashMap<String, Bitmap> {
        val questIconResIds = (
            allEditTypes.map { it.icon } +
            OsmNoteQuestType.icon +
            R.drawable.ic_quest_create_note
        ).toSortedSet()

        val result = HashMap<String, Bitmap>(questIconResIds.size)

        val scale = 2f
        val size = context.resources.dpToPx(71 * scale)
        val sizeInt = ceil(size).toInt()
        val iconSize = context.resources.dpToPx(48 * scale)
        val iconPinOffset = context.resources.dpToPx(2 * scale)
        val pinTopRightPadding = context.resources.dpToPx(5 * scale)

        val pin = context.getDrawable(R.drawable.pin)!!
        val pinShadow = context.getDrawable(R.drawable.pin_shadow)!!

        val pinWidth = (size - pinTopRightPadding) * pin.intrinsicWidth / pin.intrinsicHeight
        val pinXOffset = size - pinTopRightPadding - pinWidth

        for (questIconResId in questIconResIds) {
            val bitmap = Bitmap.createBitmap(sizeInt, sizeInt, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            pinShadow.setBounds(0, 0, sizeInt, sizeInt)
            pinShadow.draw(canvas)
            pin.bounds = RectF(
                pinXOffset,
                pinTopRightPadding,
                size - pinTopRightPadding,
                size
            ).toRect()
            pin.draw(canvas)
            val questIcon = context.getDrawable(questIconResId)!!
            questIcon.bounds = RectF(
                pinXOffset + iconPinOffset,
                pinTopRightPadding + iconPinOffset,
                pinXOffset + iconPinOffset + iconSize,
                pinTopRightPadding + iconPinOffset + iconSize
            ).toRect()
            questIcon.draw(canvas)
            val questIconName = context.resources.getResourceEntryName(questIconResId)
            result[questIconName] = bitmap
        }

        return result
    }

    private fun createPresetBitmaps(): HashMap<String, Bitmap> {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val deferredIcons = presetIconIndex.values.map { presetIconResId ->
            scope.async {
                val name = context.resources.getResourceEntryName(presetIconResId)
                val bitmap = context.getDrawable(presetIconResId)!!.createBitmap().toSdf(
                    radius = ceil(context.resources.dpToPx(2.5)).toInt()
                )
                name to bitmap
            }
        }
        val result = HashMap<String, Bitmap>(presetIconIndex.values.size)
        runBlocking { deferredIcons.awaitAll().associateTo(result) { it } }
        return result
    }

    private fun createMarkerBitmaps(): HashMap<String, Bitmap> {
        val icons = BuildingType.entries.map { it.iconResId }
        val result = HashMap<String, Bitmap>(icons.size)
        val iconSize = context.resources.dpToPx(32).toInt()
        for (iconId in icons) {
            val name = context.resources.getResourceEntryName(iconId)
            val bitmap = context.getDrawable(iconId)!!.createBitmap(iconSize, iconSize)
            result[name] = bitmap
        }
        result["crosshair_marker"] = context.getDrawable(R.drawable.crosshair_marker)!!.createBitmap()
        return result
    }
}
