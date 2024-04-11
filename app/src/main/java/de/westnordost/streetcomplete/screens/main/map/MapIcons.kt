package de.westnordost.streetcomplete.screens.main.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import androidx.core.graphics.toRect
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.AllEditTypes
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.util.ktx.createBitmap
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.view.presetIconIndex
import kotlin.math.ceil

class MapIcons(
    private val context: Context,
    private val allEditTypes: AllEditTypes,
) {
    val pinBitmaps by lazy { createPinBitmaps() }
    val presetBitmaps by lazy { createPresetBitmaps() }

    private fun createPinBitmaps(): HashMap<String, Bitmap> {
        val questIconResIds = (allEditTypes.map { it.icon } + OsmNoteQuestType.icon).toSortedSet()

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
        val result = HashMap<String, Bitmap>(presetIconIndex.values.size)
        for (presetIconResId in presetIconIndex.values) {
            val name = context.resources.getResourceEntryName(presetIconResId)
            val bitmap = context.getDrawable(presetIconResId)!!.createBitmap()
            result[name] = bitmap
        }
        return result
    }
}
