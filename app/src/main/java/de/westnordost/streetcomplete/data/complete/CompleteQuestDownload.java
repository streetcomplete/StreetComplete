package de.westnordost.streetcomplete.data.complete;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.VisibleQuestListener;
import de.westnordost.streetcomplete.data.meta.CountryBoundaries;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmElementKey;

public class CompleteQuestDownload
{
	private static final String TAG = "QuestDownload";

	// injections
	private final CompleteQuestDao completeQuestDB;
	private final Future<CountryBoundaries> countryBoundariesFuture;
	private final ElementGeometryDao geometryDB;
	private final MergedElementDao elementDB;

	// listener
	private VisibleQuestListener listener;

	@Inject public CompleteQuestDownload(CompleteQuestDao completeQuestDB,
										 FutureTask<CountryBoundaries> countryBoundariesFuture,
										 ElementGeometryDao geometryDB, MergedElementDao elementDB)
	{
		this.completeQuestDB = completeQuestDB;
		this.countryBoundariesFuture = countryBoundariesFuture;
		this.geometryDB = geometryDB;
		this.elementDB = elementDB;
	}

	public void setQuestListener(VisibleQuestListener listener)
	{
		this.listener = listener;
	}

	public boolean download(BoundingBox bbox, SimpleOverpassCompleteQuestType questType)
	{
		/*Download the quest only if there are no hidden or answered quests for this type
		(which means that this quest type has already been answered)*/
		if (!completeQuestDB.getAllByType(getQuestTypeName(questType), QuestStatus.HIDDEN).isEmpty() &&
				!completeQuestDB.getAllByType(getQuestTypeName(questType), QuestStatus.ANSWERED).isEmpty())
		{
			Log.i(TAG, getQuestTypeName(questType) + ": " +
					"Skipped because this quest type has already been answered");
			return true;
		}

		if(!checkIsEnabledFor(questType, bbox))
		{
			Log.i(TAG, getQuestTypeName(questType) + ": " +
					"Skipped because it is disabled for this country");
			return true;
		}

		final Collection<CompleteQuest> quests = new ArrayList<>();
		final ArrayList<ElementGeometryDao.Row> geometryRows = new ArrayList<>();
		final Map<OsmElementKey,Element> elements = new HashMap<>();

		long time = System.currentTimeMillis();
		boolean success = questType.download(bbox, (element, geometry) ->
		{
			if(mayCreateQuestFrom(questType, element, geometry))
			{
				Element.Type elementType = element.getType();
				long elementId = element.getId();

				Complete complete = new Complete();
				complete.apiId = questType.getApiId();
				complete.status = QuestStatus.NEW;
				complete.country = getCountryForPosition(geometry.center);
				complete.completeType = questType.getCompleteType();

				CompleteQuest quest = new CompleteQuest(complete, questType, elementType, elementId, geometry);

				geometryRows.add(new ElementGeometryDao.Row(elementType, elementId, quest.getGeometry()));
				quests.add(quest);
				OsmElementKey elementKey = new OsmElementKey(elementType, elementId);
				elements.put(elementKey, element);
			}
		});
		if(!success) return false;

		// geometry and elements must be put into DB first because quests have foreign keys on it
		//TODO: Why is the geometry not saved to the db?
		geometryDB.putAll(geometryRows);
		elementDB.putAll(elements.values());

		int newAmount = completeQuestDB.replaceAll(quests);

		if(listener != null)
		{
			Iterator<CompleteQuest> it = quests.iterator();
			while(it.hasNext())
			{
				// it is null if this quest is already in the DB, so don't call onQuestCreated
				if(it.next().getId() == null) it.remove();
			}

			if(!quests.isEmpty()) {
				listener.onQuestsCreated(quests, QuestGroup.COMPLETE);
			}
		}

		geometryDB.deleteUnreferenced();
		elementDB.deleteUnreferenced();

		Log.i(TAG, getQuestTypeName(questType) + ": " +
				"Added " + newAmount + " new CompleteStreetComplete Quests" +
				" in " + (System.currentTimeMillis() - time) + "ms");

		return true;
	}

	private String getCountryForPosition(LatLon position)
	{
		CountryBoundaries countryBoundaries;
		try
		{
			countryBoundaries = countryBoundariesFuture.get();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		return countryBoundaries.getIsoCodes(position.getLongitude(), position.getLatitude()).get(0);
	}

	private boolean mayCreateQuestFrom(CompleteQuestType questType, Element element,
									   ElementGeometry geometry)
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
		return true;
	}

	private boolean checkIsEnabledFor(CompleteQuestType questType, BoundingBox bbox)
	{
		Countries countries = questType.getEnabledForCountries();
		if(!countries.isAllCountries())
		{
			CountryBoundaries countryBoundaries;
			try
			{
				countryBoundaries = countryBoundariesFuture.get();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}

			String[] disabledCountries = countries.getDisabledCountries();
			if(disabledCountries != null)
			{
				if(countryBoundaries.intersectsWithAny(disabledCountries, bbox))
				{
					return false;
				}
			}
			String[] enabledCountries = countries.getEnabledCountries();
			if(enabledCountries != null)
			{
				if(!countryBoundaries.isInAny(enabledCountries, bbox))
				{
					return false;
				}
			}
		}
		return true;
	}

	private static String getQuestTypeName(QuestType q) { return q.getClass().getSimpleName(); }
	private static String getElementAsLogString(Element element) { return element.getType().name().toLowerCase() + " #" + element.getId(); }
}
