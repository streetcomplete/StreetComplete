package de.westnordost.streetcomplete.data.osm.download;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.inject.Inject;

import de.westnordost.countryboundaries.CountryBoundaries;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.VisibleQuestListener;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmElementKey;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;

public class OsmQuestDownload
{
	private static final String TAG = "QuestDownload";

	// injections
	private final ElementGeometryDao geometryDB;
	private final MergedElementDao elementDB;
	private final OsmQuestDao osmQuestDB;
	private final Future<CountryBoundaries> countryBoundariesFuture;

	// listener
	private VisibleQuestListener questListener;

	@Inject public OsmQuestDownload(
			ElementGeometryDao geometryDB, MergedElementDao elementDB, OsmQuestDao osmQuestDB,
			FutureTask<CountryBoundaries> countryBoundariesFuture)
	{
		this.geometryDB = geometryDB;
		this.elementDB = elementDB;
		this.osmQuestDB = osmQuestDB;
		this.countryBoundariesFuture = countryBoundariesFuture;
	}

	public void setQuestListener(VisibleQuestListener listener)
	{
		this.questListener = listener;
	}

	public boolean download(final OsmElementQuestType questType, BoundingBox bbox,
							final Set<LatLon> blacklistedPositions)
	{
		if(!isQuestTypeEnabledForBoundingBox(questType, bbox))
		{
			Log.i(TAG, getQuestTypeName(questType) + ": " +
				"Skipped because it is disabled for this country");
			return true;
		}
		Log.i(TAG, getQuestTypeName(questType) + ": Starting");

		final ArrayList<ElementGeometryDao.Row> geometryRows = new ArrayList<>();
		final Map<OsmElementKey,Element> elements = new HashMap<>();
		final ArrayList<OsmQuest> quests = new ArrayList<>();
		final Map<OsmElementKey, Long> previousQuests = getPreviousQuestsIdsByElementKey(questType, bbox);

		long time = System.currentTimeMillis();
		boolean success = questType.download(bbox, (element, geometry) ->
		{
			if(mayCreateQuestFrom(questType, element, geometry, blacklistedPositions))
			{
				Element.Type elementType = element.getType();
				long elementId = element.getId();

				OsmQuest quest = new OsmQuest(questType, elementType, elementId, geometry);

				geometryRows.add(new ElementGeometryDao.Row(
						elementType, elementId, quest.getGeometry()));
				quests.add(quest);
				OsmElementKey elementKey = new OsmElementKey(elementType, elementId);
				elements.put(elementKey, element);
				previousQuests.remove(elementKey);
			}
		});
		if(!success) return false;

		// geometry and elements must be put into DB first because quests have foreign keys on it
		geometryDB.putAll(geometryRows);
		elementDB.putAll(elements.values());

		int newQuestsByQuestType = osmQuestDB.addAll(quests);

		if(questListener != null)
		{
			Iterator<OsmQuest> it = quests.iterator();
			while(it.hasNext())
			{
				// it is null if this quest is already in the DB, so don't call onQuestCreated
				if(it.next().getId() == null) it.remove();
			}
			if(!quests.isEmpty()) questListener.onQuestsCreated(quests, QuestGroup.OSM);
		}

		if(!previousQuests.isEmpty())
		{
			if(questListener != null)
			{
				questListener.onQuestsRemoved(previousQuests.values(), QuestGroup.OSM);
			}

			osmQuestDB.deleteAll(previousQuests.values());
		}

		// note: this could be done after ALL osm quest types have been downloaded if this
		// turns out to be slow if done for every quest type
		geometryDB.deleteUnreferenced();
		elementDB.deleteUnreferenced();

		int obsoleteAmount = previousQuests.size();
		Log.i(TAG, getQuestTypeName(questType) + ": " +
				"Added " + newQuestsByQuestType + " new and " +
				"removed " + obsoleteAmount + " already resolved quests." +
				" (Total: " + quests.size() + ")" +
				" in " + ((System.currentTimeMillis() - time)/1000) + "s");

		return true;
	}

	private Map<OsmElementKey, Long> getPreviousQuestsIdsByElementKey(
			OsmElementQuestType questType, BoundingBox bbox)
	{
		String questTypeName = questType.getClass().getSimpleName();
		Map<OsmElementKey, Long> result = new HashMap<>();
		for(OsmQuest quest : osmQuestDB.getAll(bbox, null, questTypeName, null, null))
		{
			result.put(new OsmElementKey(quest.getElementType(), quest.getElementId()),	quest.getId());
		}
		return result;
	}

	private boolean isQuestTypeEnabledForBoundingBox(OsmElementQuestType questType, BoundingBox bbox)
	{
		Countries countries = questType.getEnabledForCountries();
		if(countries.isAllCountries()) return true;

		if(countries.isAllExcept())
		{
			Set<String> containingCountries = getCountryBoundaries().getContainingIds(
				bbox.getMinLongitude(), bbox.getMinLatitude(),
				bbox.getMaxLongitude(), bbox.getMaxLatitude());

			return !containsAnyOf(containingCountries, countries.getExceptions());
		}
		else
		{
			Set<String> intersectingCountries = getCountryBoundaries().getIntersectingIds(
				bbox.getMinLongitude(), bbox.getMinLatitude(),
				bbox.getMaxLongitude(), bbox.getMaxLatitude());

			return containsAnyOf(intersectingCountries, countries.getExceptions());
		}
	}

	private boolean mayCreateQuestFrom(
		OsmElementQuestType questType, Element element, ElementGeometry geometry,
		Set<LatLon> blacklistedPositions)
	{
		// invalid geometry -> can't show this quest, so skip it
		if(geometry == null)
		{
			// classified as warning because it might very well be a bug on the geometry
			// creation on our side
			Log.w(TAG, getQuestTypeName(questType) + ": Not adding a quest " +
					" because the element " + getElementAsLogString(element) +
					" has no valid geometry");
			return false;
		}

		// do not create quests whose marker is at a blacklisted position
		if(blacklistedPositions != null && blacklistedPositions.contains(geometry.center))
		{
			Log.d(TAG, getQuestTypeName(questType) + ": Not adding a quest at " +
					getPosAsLogString(geometry.center) +
					" because there is a note at that position");
			return false;
		}

		// do not create quests in countries where the quest is not activated
		Countries countries = questType.getEnabledForCountries();
		if(!countries.isAllCountries())
		{
			LatLon p = geometry.center;

			if(getCountryBoundaries().isInAny(p.getLongitude(), p.getLatitude(), countries.getExceptions())
				== countries.isAllExcept())
			{
				Log.v(TAG, getQuestTypeName(questType) + ": Not adding a quest at " +
					getPosAsLogString(geometry.center) +
					" because the quest is disabled in this country");
				return false;
			}
		}

		return true;
	}

	private CountryBoundaries getCountryBoundaries()
	{
		try
		{
			return countryBoundariesFuture.get();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/** @return true if any item of second collection b is contained in first collection a */
	private static boolean containsAnyOf(Collection a, Collection b)
	{
		for (Object o : b)
		{
			if(a.contains(o)) return true;
		}
		return false;
	}

	private static String getElementAsLogString(Element element)
	{
		return element.getType().name().toLowerCase() + " #" + element.getId();
	}

	private static String getQuestTypeName(QuestType q)
	{
		return q.getClass().getSimpleName();
	}

	private static String getPosAsLogString(LatLon pos)
	{
		return pos.getLatitude() + ", " + pos.getLongitude();
	}
}
