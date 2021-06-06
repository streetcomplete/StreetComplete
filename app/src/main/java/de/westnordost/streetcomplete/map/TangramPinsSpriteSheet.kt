package de.westnordost.streetcomplete.map

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.edit
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil
import kotlin.math.sqrt

/** From all the quest types, creates and saves a sprite sheet of quest type pin icons, provides
 *  the scene updates for tangram to access this sprite sheet  */
@Singleton class TangramPinsSpriteSheet @Inject constructor(
    private val context: Context,
    private val questTypeRegistry: QuestTypeRegistry,
    private val prefs: SharedPreferences
) {
    val sceneUpdates: List<Pair<String, String>> by lazy {
        val isSpriteSheetCurrent = prefs.getInt(Prefs.PIN_SPRITES_VERSION, 0) == BuildConfig.VERSION_CODE

        val spriteSheet =
            if (isSpriteSheetCurrent && !BuildConfig.DEBUG)
                prefs.getString(Prefs.PIN_SPRITES, "")!!
            else
                createSpritesheet()

        createSceneUpdates(spriteSheet)
    }

    private fun createSpritesheet(): String {
        val questIconResIds = (questTypeRegistry.all.map { it.icon } + ADDITIONAL_ICONS).toSortedSet()

        val spriteSheetEntries: MutableList<String> = ArrayList(questIconResIds.size)
        val questPin = context.resources.getDrawable(R.drawable.pin)
        val iconSize = questPin.intrinsicWidth
        val questIconSize = 2 * iconSize / 3
        val questIconOffsetX = 56 * iconSize / 192
        val questIconOffsetY = 18 * iconSize / 192
        val sheetSideLength = ceil(sqrt(questIconResIds.size.toDouble())).toInt()
        val bitmapLength = sheetSideLength * iconSize
        val spriteSheet = Bitmap.createBitmap(bitmapLength, bitmapLength, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(spriteSheet)

        for ((i, questIconResId) in questIconResIds.withIndex()) {
            val x = i % sheetSideLength * iconSize
            val y = i / sheetSideLength * iconSize
            questPin.setBounds(x, y, x + iconSize, y + iconSize)
            questPin.draw(canvas)
            val questIcon = context.resources.getDrawable(questIconResId)
            val questX = x + questIconOffsetX
            val questY = y + questIconOffsetY
            questIcon.setBounds(questX, questY, questX + questIconSize, questY + questIconSize)
            questIcon.draw(canvas)
            val questIconName = context.resources.getResourceEntryName(questIconResId)
            spriteSheetEntries.add("$questIconName: [$x,$y,$iconSize,$iconSize]")
        }

        context.deleteFile(PIN_ICONS_FILE)
        val spriteSheetIconsFile = context.openFileOutput(PIN_ICONS_FILE, Context.MODE_PRIVATE)
        spriteSheet.compress(Bitmap.CompressFormat.PNG, 0, spriteSheetIconsFile)
        spriteSheetIconsFile.close()

        val questSprites = "{${spriteSheetEntries.joinToString(",")}}"

        prefs.edit {
            putInt(Prefs.PIN_SPRITES_VERSION, BuildConfig.VERSION_CODE)
            putString(Prefs.PIN_SPRITES, questSprites)
        }

        return questSprites
    }

    private fun createSceneUpdates(pinSprites: String): List<Pair<String, String>> = listOf(
        "textures.pins.url" to "file://${context.filesDir}/$PIN_ICONS_FILE",
        "textures.pins.sprites" to pinSprites
    )

    companion object {
        private const val PIN_ICONS_FILE = "pins.png"
        private val ADDITIONAL_ICONS = listOf(
            R.drawable.ic_quest_create_note
        )
    }
}
