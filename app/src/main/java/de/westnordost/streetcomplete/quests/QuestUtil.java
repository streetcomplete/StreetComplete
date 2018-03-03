package de.westnordost.streetcomplete.quests;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;

import java.util.Collections;
import java.util.Map;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;

public class QuestUtil
{
	public static String getTitle(
		@NonNull Resources r, @NonNull QuestType questType, @Nullable Element element)
	{
		Map<String,String> tags = getTags(element);
		return r.getString(getQuestTitleResId(questType, tags), getElementName(tags));
	}

	public static Spanned getHtmlTitle(
		@NonNull Resources r, @NonNull QuestType questType, @Nullable Element element)
	{
		Map<String,String> tags = getTags(element);
		String name = getElementName(tags);
		String spanName = name != null ? "<i>"+Html.escapeHtml(name)+"</i>" : null;

		return Html.fromHtml(r.getString(getQuestTitleResId(questType, tags), spanName));
	}

	private static Map<String,String> getTags(@Nullable Element element)
	{
		return element != null && element.getTags() != null ? element.getTags() : Collections.emptyMap();
	}

	private static int getQuestTitleResId(QuestType questType, Map<String,String> tags)
	{
		if(questType instanceof OsmElementQuestType)
		{
			return ((OsmElementQuestType) questType).getTitle(tags);
		}
		return questType.getTitle();
	}

	private static String getElementName(Map<String,String> tags)
	{
		return tags.get("name");
	}
}
