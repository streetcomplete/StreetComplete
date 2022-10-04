package de.westnordost.streetcomplete.screens.main.map

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.content.edit
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.createBitmapWithWhiteBorder
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.view.presetIconIndex
import java.lang.Integer.max
import kotlin.math.ceil
import kotlin.math.sqrt

/** Creates and saves a sprite sheet of icons used in overlays, provides
 *  the scene updates for tangram to access this sprite sheet  */
class TangramIconsSpriteSheet(
    private val context: Context,
    private val prefs: SharedPreferences,
) {
    val sceneUpdates: List<Pair<String, String>> by lazy {
        val isSpriteSheetCurrent = prefs.getInt(Prefs.ICON_SPRITES_VERSION, 0) == BuildConfig.VERSION_CODE
        val spriteSheet = when {
            !isSpriteSheetCurrent || BuildConfig.DEBUG -> createSpritesheet()
            else -> prefs.getString(Prefs.ICON_SPRITES, "")!!
        }

        createSceneUpdates(spriteSheet)
    }

    private fun createSpritesheet(): String {
        val iconResIds = ICONS.toSortedSet()
        val iconSize = context.dpToPx(26).toInt()
        val borderWidth = context.dpToPx(3).toInt()
        val safePadding = context.dpToPx(2).toInt()
        val size = iconSize + borderWidth * 2 + safePadding

        val spriteSheetEntries: MutableList<String> = ArrayList(iconResIds.size)
        val sheetSideLength = ceil(sqrt(iconResIds.size.toDouble())).toInt()
        val spriteSheet = Bitmap.createBitmap(size * sheetSideLength, size * sheetSideLength, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(spriteSheet)

        for ((i, iconResId) in iconResIds.withIndex()) {
            val x = i % sheetSideLength * size
            val y = i / sheetSideLength * size
            val iconName = context.resources.getResourceEntryName(iconResId)
            val icon = context.getDrawable(iconResId)!!
            if (iconName.startsWith("ic_preset_")) {
                icon.setTint(Color.BLACK)
            }
            val iconIntrinsicSize = max(icon.intrinsicWidth, icon.intrinsicHeight)
            val iconWidth = iconSize * icon.intrinsicWidth / iconIntrinsicSize
            val iconHeight = iconSize * icon.intrinsicHeight / iconIntrinsicSize
            val bitmap = icon.createBitmapWithWhiteBorder(borderWidth, iconWidth, iconHeight)
            val padX = (iconSize - iconWidth) / 2f
            val padY = (iconSize - iconHeight) / 2f
            canvas.drawBitmap(bitmap, padX + x.toFloat(), padY + y.toFloat(), null)
            spriteSheetEntries.add("$iconName: [$x,$y,$size,$size]")
        }

        context.deleteFile(ICONS_FILE)
        val spriteSheetIconsFile = context.openFileOutput(ICONS_FILE, Context.MODE_PRIVATE)
        spriteSheet.compress(Bitmap.CompressFormat.PNG, 0, spriteSheetIconsFile)
        spriteSheetIconsFile.close()

        val sprites = "{${spriteSheetEntries.joinToString(",")}}"

        prefs.edit {
            putInt(Prefs.ICON_SPRITES_VERSION, BuildConfig.VERSION_CODE)
            putString(Prefs.ICON_SPRITES, sprites)
        }

        return sprites
    }

    private fun createSceneUpdates(pinSprites: String): List<Pair<String, String>> = listOf(
        "textures.icons.url" to "file://${context.filesDir}/$ICONS_FILE",
        "textures.icons.sprites" to pinSprites
    )

    companion object {
        private const val ICONS_FILE = "icons.png"
        private val ICONS = listOf(
            R.drawable.ic_pin_choker_borderless,
            R.drawable.ic_pin_parking_borderless
        )  + presetIconIndex.values
    }
}
