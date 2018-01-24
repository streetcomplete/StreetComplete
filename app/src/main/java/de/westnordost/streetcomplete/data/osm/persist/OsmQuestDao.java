package de.westnordost.streetcomplete.data.osm.persist;

import android.database.sqlite.SQLiteOpenHelper;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.data.WhereSelectionBuilder;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.util.Serializer;

public class OsmQuestDao extends AOsmQuestDao
{

	@Inject public OsmQuestDao(SQLiteOpenHelper dbHelper, Serializer serializer, QuestTypeRegistry questTypeList)
	{
		super(dbHelper, serializer, questTypeList);
	}

	@Override protected String getTableName() { return OsmQuestTable.NAME; }
	@Override protected String getMergedViewName() { return OsmQuestTable.NAME_MERGED_VIEW; }

	public OsmQuest getNextNewAt(long questId, final List<String> questTypesNames)
	{
		OsmQuest quest = get(questId);
		if(quest == null) return null;

		WhereSelectionBuilder qb = new WhereSelectionBuilder();
		addQuestStatus(QuestStatus.NEW, qb);
		addElementType(quest.getElementType(), qb);
		addElementId(quest.getElementId(), qb);
		addQuestTypes(questTypesNames, qb);

		List<OsmQuest> allNext = getAllThings(getMergedViewName(), null, qb, this::createObjectFrom);
		Collections.sort(allNext, (o1, o2) ->
		{
			String o1Name = o1.getType().getClass().getSimpleName();
			String o2Name = o2.getType().getClass().getSimpleName();
			return questTypesNames.indexOf(o1Name) - questTypesNames.indexOf(o2Name);
		});

		return allNext.isEmpty() ? null : allNext.get(0);
	}
}
