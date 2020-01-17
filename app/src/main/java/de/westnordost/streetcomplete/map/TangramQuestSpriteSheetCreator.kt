package de.westnordost.streetcomplete.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import com.mapzen.tangram.SceneUpdate
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.QuestTypeRegistry
import java.util.*
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.sqrt

class TangramQuestSpriteSheetCreator @Inject constructor(
    private val context: Context,
    private val questTypeRegistry: QuestTypeRegistry
) {
    private val sceneUpdates: List<SceneUpdate> by lazy {
	    val allIconIds = questTypeRegistry.all.map { it.icon }.toSet()
	    create(allIconIds)
    }

	fun get(): List<SceneUpdate> = sceneUpdates

    private fun create(questIconResIds: Collection<Int>): List<SceneUpdate> {
        val spriteSheetEntries: MutableList<String> = ArrayList(questIconResIds.size)
        val questPin = context.resources.getDrawable(R.drawable.quest_pin)
        val iconSize = questPin.intrinsicWidth
        val questIconSize = 2 * iconSize / 3
        val questIconOffsetX = 56 * iconSize / 192
        val questIconOffsetY = 16 * iconSize / 192
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

        context.deleteFile(QUEST_ICONS_FILE)
        val spriteSheetIconsFile = context.openFileOutput(QUEST_ICONS_FILE, Context.MODE_PRIVATE)
        spriteSheet.compress(Bitmap.CompressFormat.PNG, 0, spriteSheetIconsFile)
        spriteSheetIconsFile.close()

        return listOf(
            SceneUpdate("textures.quests.url", "file://${context.filesDir}/$QUEST_ICONS_FILE"),
            SceneUpdate("textures.quests.sprites", "{${spriteSheetEntries.joinToString(", ")}}")
        )
    }

    companion object {
        private const val QUEST_ICONS_FILE = "quests.png"
    }
}
