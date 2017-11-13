package de.westnordost.streetcomplete.tangram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.mapzen.tangram.SceneUpdate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;

public class TangramQuestSpriteSheetCreator
{
	private static final String QUEST_ICONS_FILE = "quests.png";

	private final Context context;
	private final QuestTypeRegistry questTypeRegistry;

	private List<SceneUpdate> sceneUpdates;

	@Inject public TangramQuestSpriteSheetCreator(Context context, QuestTypeRegistry questTypeRegistry)
	{
		this.context = context;
		this.questTypeRegistry = questTypeRegistry;
	}

	public synchronized List<SceneUpdate> get()
	{
		if(sceneUpdates == null) {
			sceneUpdates = create(getQuestIconResourceIds());
		}
		return sceneUpdates;
	}

	private Set<Integer> getQuestIconResourceIds()
	{
		List<QuestType> questTypeList = questTypeRegistry.getAll();
		Set<Integer> questIconResIds = new HashSet<>(questTypeList.size());
		for(QuestType questType : questTypeList)
		{
			questIconResIds.add(questType.getIcon());
		}
		return questIconResIds;
	}

	private List<SceneUpdate> create(Collection<Integer> questIconResIds)
	{
		List<String> spriteSheetEntries = new ArrayList<>();

		Drawable questPin = context.getResources().getDrawable(R.drawable.quest_pin);
		int iconSize = questPin.getIntrinsicWidth();
		int questIconSize = 2 * iconSize / 3;
		int questIconOffsetX = 56 * iconSize / 192;
		int questIconOffsetY = 16 * iconSize / 192;
		int sheetSideLength = (int) Math.ceil(Math.sqrt(questIconResIds.size()));
		int bitmapLength = sheetSideLength * iconSize;

		Bitmap spriteSheet = Bitmap.createBitmap(bitmapLength, bitmapLength, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(spriteSheet);

		int i = 0;
		for (int questIconResId : questIconResIds)
		{
			int x = (i % sheetSideLength) * iconSize;
			int y = (i / sheetSideLength) * iconSize;

			questPin.setBounds(x, y, x + iconSize, y + iconSize);
			questPin.draw(canvas);

			Drawable questIcon = context.getResources().getDrawable(questIconResId);
			int questX = x + questIconOffsetX;
			int questY = y + questIconOffsetY;
			questIcon.setBounds(questX, questY, questX + questIconSize, questY + questIconSize);
			questIcon.draw(canvas);

			String questIconName = context.getResources().getResourceEntryName(questIconResId);
			spriteSheetEntries.add(String.format(Locale.US,"%s: [%d,%d,%d,%d]",
					questIconName, x, y, iconSize, iconSize));
			++i;
		}

		String sprites = "{" + TextUtils.join(", ", spriteSheetEntries) + "}";

		try
		{
			context.deleteFile(QUEST_ICONS_FILE);
			FileOutputStream spriteSheetIconsFile = context.openFileOutput(QUEST_ICONS_FILE, Context.MODE_PRIVATE);
			spriteSheet.compress(Bitmap.CompressFormat.PNG, 0, spriteSheetIconsFile);
			spriteSheetIconsFile.close();
		}
		catch (IOException e)
		{
			// an IOException here is unexpected ;-)
			throw new RuntimeException(e);
		}

		List<SceneUpdate> updates = new ArrayList<>(2);
		updates.add(new SceneUpdate("textures.quests.url", "file://" + context.getFilesDir()+"/"+ QUEST_ICONS_FILE));
		updates.add(new SceneUpdate("textures.quests.sprites", sprites));
		return updates;
	}
}
