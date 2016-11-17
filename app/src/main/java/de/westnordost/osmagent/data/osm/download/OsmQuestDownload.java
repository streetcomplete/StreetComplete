package de.westnordost.osmagent.data.osm.download;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import de.westnordost.osmagent.data.QuestType;
import de.westnordost.osmagent.data.QuestTypes;
import de.westnordost.osmagent.data.osm.ElementGeometry;
import de.westnordost.osmagent.data.osm.OsmQuest;
import de.westnordost.osmagent.data.osm.OverpassQuestType;
import de.westnordost.osmagent.data.osm.persist.ElementGeometryDao;
import de.westnordost.osmagent.data.osm.persist.MergedElementDao;
import de.westnordost.osmagent.data.osm.persist.OsmQuestDao;
import de.westnordost.osmagent.data.osm.persist.OsmElementKey;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;

// TODO test case
public class OsmQuestDownload
{
	private static final String TAG = "QuestDownload";

	// injections
	private final OverpassMapDataDao overpassServer;
	private final ElementGeometryDao geometryDB;
	private final MergedElementDao elementDB;
	private final OsmQuestDao osmQuestDB;
	private final QuestTypes questTypeList;

	// listener
	private ProgressListener progressListener;
	public interface ProgressListener
	{
		void onProgress(float progress, QuestType questType);
	}

	// state
	private boolean called = false;

	private int visibleQuests;
	private int downloadedQuestTypes;

	// args
	private BoundingBox bbox;
	private Set<LatLon> blacklistedPositions;
	private Integer maxVisibleQuests;

	@Inject public OsmQuestDownload(
			OverpassMapDataDao overpassServer, ElementGeometryDao geometryDB,
			MergedElementDao elementDB, OsmQuestDao osmQuestDB,
			QuestTypes questTypeList)
	{
		this.overpassServer = overpassServer;
		this.geometryDB = geometryDB;
		this.elementDB = elementDB;
		this.osmQuestDB = osmQuestDB;
		this.questTypeList = questTypeList;
	}

	public void setProgressListener(ProgressListener progressListener)
	{
		this.progressListener = progressListener;
	}

	public void download(BoundingBox bbox, Set<LatLon> blacklistedPositions,
						 Integer maxVisibleQuests, AtomicBoolean cancelState)
	{
		if(called) throw new IllegalStateException("May only be called once");
		else called = true;

		this.bbox = bbox;
		this.blacklistedPositions = blacklistedPositions;
		this.maxVisibleQuests = maxVisibleQuests;

		List<QuestType> questTypes = questTypeList.getQuestTypesSortedByImportance();

		try
		{
			for(QuestType questType : questTypes)
			{
				if(!(questType instanceof OverpassQuestType)) continue;
				if(cancelState.get()) break;
				if(maxVisibleQuests != null && visibleQuests >= maxVisibleQuests) break;

				downloadQuestType((OverpassQuestType) questType);
			}
		}
		finally
		{
			geometryDB.deleteUnreferenced();
			elementDB.deleteUnreferenced();
		}
	}

	private Map<OsmElementKey, Long> getPreviousQuestsIdsByElementKey(OverpassQuestType questType)
	{
		String questTypeName = questType.getClass().getSimpleName();
		Map<OsmElementKey, Long> result = new HashMap<>();
		for(OsmQuest quest : osmQuestDB.getAll(bbox, null, questTypeName, null, null))
		{
			result.put(new OsmElementKey(quest.getElementType(), quest.getElementId()),	quest.getId());
		}
		return result;
	}

	private void downloadQuestType(final OverpassQuestType questType)
	{
		final ArrayList<ElementGeometryDao.Row> geometryRows = new ArrayList<>();
		final ArrayList<Element> elements = new ArrayList<>();
		final ArrayList<OsmQuest> quests = new ArrayList<>();
		final Map<OsmElementKey, Long> previousQuests = getPreviousQuestsIdsByElementKey(questType);

		String oql = questType.getOverpassQuery(bbox);
		overpassServer.get(oql, new MapDataWithGeometryHandler()
		{
			@Override public void handle(Element element, ElementGeometry geometry)
			{
				if(!mayCreateQuestFrom(questType, element, geometry)) return;

				Element.Type elementType = element.getType();
				long elementId = element.getId();

				geometryRows.add(new ElementGeometryDao.Row(elementType, elementId, geometry));
				elements.add(element);
				quests.add(new OsmQuest(questType, elementType, elementId, geometry));

				previousQuests.remove(new OsmElementKey(elementType, elementId));
			}
		});

		// geometry and elements must be put into DB first because quests have foreign keys on it
		geometryDB.putAll(geometryRows);
		elementDB.putAll(elements);

		int newQuestsByQuestType = osmQuestDB.addAll(quests);

		if(!previousQuests.isEmpty())
		{
			osmQuestDB.deleteAll(previousQuests.values());
		}

		int visibleQuestsByQuestType = quests.size();
		int obsoleteAmount = previousQuests.size();
		Log.i(TAG, getQuestTypeAsLogString(questType) + ": " +
				"Added " + newQuestsByQuestType + " new and " +
				"removed " + obsoleteAmount + " already resolved quests." +
				" (Total: " + visibleQuestsByQuestType + ")");

		visibleQuests += visibleQuestsByQuestType;
		++downloadedQuestTypes;
		dispatchProgress(questType);
	}

	private boolean mayCreateQuestFrom(OverpassQuestType questType, Element element, ElementGeometry geometry)
	{
		if(!questType.appliesTo(element)) return false;

		// invalid geometry -> can't show this quest, so skip it
		if(geometry == null)
		{
			// classified as warning because it might very well be a bug on the geometry
			// creation on our side
			Log.w(TAG, getQuestTypeAsLogString(questType) + ": Not adding a quest " +
					" because the element " + getElementAsLogString(element) +
					" has no valid geometry");
			return false;
		}

		// do not create quests whose marker is at a blacklisted position
		if(blacklistedPositions != null && blacklistedPositions.contains(geometry.center))
		{
			Log.v(TAG, getQuestTypeAsLogString(questType) + ": Not adding a quest at " +
					getPosAsLogString(geometry.center) +
					" because there is a note at that position");
			return false;
		}
		return true;
	}

	private void dispatchProgress(OverpassQuestType questType)
	{
		if(progressListener == null) return;

		int questTypes = questTypeList.getAmount();

		float progressByQuestTypes = (float) downloadedQuestTypes / questTypes;

		float progressByMaxVisible = 0;
		if(maxVisibleQuests != null)
		{
			progressByMaxVisible = (float) visibleQuests / maxVisibleQuests;
		}

		float progress = Math.min(1f, Math.max(progressByQuestTypes, progressByMaxVisible));
		progressListener.onProgress(progress, questType);
	}


	private static String getElementAsLogString(Element element)
	{
		return element.getType().name().toLowerCase() + " #" + element.getId();
	}

	private static String getQuestTypeAsLogString(QuestType q)
	{
		return q.getClass().getSimpleName();
	}

	private static String getPosAsLogString(LatLon pos)
	{
		return pos.getLatitude() + ", " + pos.getLongitude();
	}
}
