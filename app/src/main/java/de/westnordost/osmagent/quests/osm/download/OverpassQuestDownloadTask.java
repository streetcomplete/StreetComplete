package de.westnordost.osmagent.quests.osm.download;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.osm.OsmQuest;
import de.westnordost.osmagent.quests.osm.persist.ElementGeometryDao;
import de.westnordost.osmagent.quests.osm.persist.MergedElementDao;
import de.westnordost.osmagent.quests.osm.persist.OsmQuestDao;
import de.westnordost.osmagent.quests.osm.types.OverpassQuestType;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;

public class OverpassQuestDownloadTask implements Runnable
{
	@Inject OverpassMapDataDao overpassMapDataDao;

	@Inject ElementGeometryDao geometryDB;
	@Inject OsmQuestDao questDB;
	@Inject MergedElementDao elementDB;

	public OverpassQuestType questType;
	public BoundingBox bbox;

	@Inject public OverpassQuestDownloadTask() { }

	@Override public void run()
	{
		if(questType == null) throw new IllegalStateException("QuestType must be set");
		if(bbox == null) throw new IllegalStateException("Bounding box must be set");

		String oql = questType.getOverpassQuery(bbox);
		overpassMapDataDao.get(oql, new CreateOsmQuestMapDataHandler(questType,
				new Handler<OsmQuest>()
				{
					@Override public void handle(OsmQuest quest)
					{
						// geometry must be put into DB first because quest has a foreign key on it
						geometryDB.put(quest.getElementType(), quest.getElementId(), quest.getGeometry());
						questDB.add(quest);
					}
				},
				new Handler<Element>()
				{
					@Override public void handle(Element element)
					{
						elementDB.put(element);
					}
				}
		));
	}
}
