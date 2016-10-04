package de.westnordost.osmagent.quests.osm.download;


import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.QuestListener;
import de.westnordost.osmagent.quests.osm.OsmQuest;
import de.westnordost.osmagent.quests.osm.persist.ElementGeometryDao;
import de.westnordost.osmagent.quests.osm.persist.MergedElementDao;
import de.westnordost.osmagent.quests.osm.persist.OsmQuestDao;
import de.westnordost.osmagent.quests.osm.persist.OsmElementKey;
import de.westnordost.osmagent.quests.osm.types.OverpassQuestType;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;

// TODO test case
public class OsmQuestDownload
{
	public static final String OSM_QUEST_PACKAGE = "de.westnordost.osmagent.quests.osm.types";

	private final OverpassMapDataDao overpassServer;
	private final ElementGeometryDao geometryDB;
	private final MergedElementDao elementDB;
	private final OsmQuestDao osmQuestDB;

	private QuestListener questListener;
	private int visibleAmount;

	@Inject public OsmQuestDownload(
			OverpassMapDataDao overpassServer, ElementGeometryDao geometryDB,
			MergedElementDao elementDB, OsmQuestDao osmQuestDB)
	{
		this.overpassServer = overpassServer;
		this.geometryDB = geometryDB;
		this.elementDB = elementDB;
		this.osmQuestDB = osmQuestDB;
	}

	public void setQuestListener(QuestListener questListener)
	{
		this.questListener = questListener;
	}

	public void download(BoundingBox bbox, Set<LatLon> blacklistedPositions,
						 Integer maxVisibleAmount, AtomicBoolean cancelState)
	{
		List<OverpassQuestType> questTypes = ReflectionQuestTypeListCreator
				.create(OverpassQuestType.class, OSM_QUEST_PACKAGE);

		for(OverpassQuestType questType : questTypes)
		{
			if(cancelState.get()) break;
			if(maxVisibleAmount != null && visibleAmount >= maxVisibleAmount) break;

			final Map<OsmElementKey, OsmQuest> oldQuestsByElementKey = new HashMap<>();
			for(OsmQuest quest : osmQuestDB.getAll(bbox, null, questType))
			{
				oldQuestsByElementKey.put(
						new OsmElementKey(quest.getElementType(), quest.getElementId()),
						quest);
			}

			String oql = questType.getOverpassQuery(bbox);

			overpassServer.get(oql, new CreateOsmQuestMapDataHandler(questType,
					new Handler<OsmQuest>()
					{
						@Override public void handle(OsmQuest quest)
						{
							// geometry must be put into DB first because quest has a foreign key on it
							geometryDB.put(quest.getElementType(), quest.getElementId(), quest.getGeometry());
							if(osmQuestDB.add(quest))
							{
								if(questListener != null)
								{
									questListener.onQuestCreated(quest);
								}
							}
							++visibleAmount;

							oldQuestsByElementKey.remove(
									new OsmElementKey(quest.getElementType(), quest.getElementId()));
						}
					},
					new Handler<Element>()
					{
						@Override public void handle(Element element)
						{
							elementDB.put(element);
						}
					}
					, blacklistedPositions));

			removeObsoleteQuests(oldQuestsByElementKey.values());
		}

		geometryDB.deleteUnreferenced();
		elementDB.deleteUnreferenced();
	}

	private void removeObsoleteQuests(Collection<OsmQuest> oldQuests)
	{
		if(!oldQuests.isEmpty())
		{
			for (OsmQuest quest : oldQuests)
			{
				if(osmQuestDB.delete(quest.getId()))
				{
					if(questListener != null)
					{
						questListener.onQuestRemoved(quest);
					}
				}
			}
		}
	}
}
