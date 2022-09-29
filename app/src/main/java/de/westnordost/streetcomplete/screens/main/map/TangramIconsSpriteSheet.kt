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
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.view.presetIconIndex
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
        val background = context.getDrawable(R.drawable.background_pin)!!
        val backgroundSize = context.dpToPx(28).toInt()

        val iconResIds = ICONS.toSortedSet()
        val iconSize = context.dpToPx(24).toInt()
        val iconOffset = (backgroundSize - iconSize)/2

        val spriteSheetEntries: MutableList<String> = ArrayList(iconResIds.size)
        val sheetSideLength = ceil(sqrt(iconResIds.size.toDouble())).toInt()
        val bitmapLength = sheetSideLength * backgroundSize
        val spriteSheet = Bitmap.createBitmap(bitmapLength, bitmapLength, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(spriteSheet)

        for ((i, iconResId) in iconResIds.withIndex()) {
            val x = i % sheetSideLength * backgroundSize
            val y = i / sheetSideLength * backgroundSize
            val icon = context.getDrawable(iconResId)!!
            icon.setTint(Color.BLACK)
            background.setBounds(x, y, x + backgroundSize, y + backgroundSize)
            background.draw(canvas)
            val iconX = x + iconOffset
            val iconY = y + iconOffset
            icon.setBounds(iconX, iconY, iconX + iconSize, iconY + iconSize)
            icon.draw(canvas)
            val iconName = context.resources.getResourceEntryName(iconResId)
            spriteSheetEntries.add("$iconName: [$x,$y,$backgroundSize,$backgroundSize]")
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
            R.drawable.ic_pin_choker_borderless
        )  + presetIconIndex.values
    }
}
