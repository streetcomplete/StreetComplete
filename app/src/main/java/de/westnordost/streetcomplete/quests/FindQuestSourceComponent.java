package de.westnordost.streetcomplete.quests;

import android.app.Activity;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao;
import de.westnordost.streetcomplete.util.JTSConst;
import de.westnordost.streetcomplete.util.SphericalEarthMath;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

/** Finds the source (as in: from survey or from knowledge) of a quest solution, either by looking
 *  at the GPS position or asking the user */
public class FindQuestSourceComponent
{
	/* Considerations for choosing these values:
	*  - users should be encouraged to *really* go right there and check even if they think they
	*    see it from afar already
	*
	*  - just having walked by something should though still count as survey though. (It might be
	*    inappropriate or awkward to stop and flip out the smartphone directly there)
	*
	*  - GPS position might not be updated right after they fetched it out of their pocket, but GPS
	*    position should be reset to "unknown" (instead of "wrong") when switching back to the app
	*
	*  - the distance is the minimum distance between the quest geometry (i.e. a road) and the line
	*    between the user's position when he opened the quest form and the position when he pressed
	*    "ok", MINUS the current GPS accuracy, so it is a pretty forgiving calculation already
	* */
	private static final float MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY = 80; //m

	private static final String
			SURVEY = "survey",
			LOCAL_KNOWLEGE = "local knowledge";
	private static boolean dontShowAgain = false;
	private static int timesShown = 0;

	private Activity activity;
	private final OsmQuestDao osmQuestDB;
	private final OsmNoteQuestDao osmNoteQuestDao;

	private final GeometryFactory geometryFactory = new GeometryFactory();

	public interface Listener
	{
		void onFindQuestSourceResult(String source);
	}

	@Inject public FindQuestSourceComponent(OsmQuestDao osmQuestDB, OsmNoteQuestDao osmNoteQuestDao)
	{
		this.osmQuestDB = osmQuestDB;
		this.osmNoteQuestDao = osmNoteQuestDao;
	}

	public void onCreate(Activity context)
	{
		this.activity = context;
	}

	public void findSource(final long questId, final QuestGroup group, final Location[] locations,
						   final Listener listener)
	{
		Double distance = getDistanceToElementInMeters(questId, group, locations);
		if(dontShowAgain || distance != null && distance < MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY)
		{
			listener.onFindQuestSourceResult(SURVEY);
		}
		else
		{
			View inner = LayoutInflater.from(activity).inflate(
					R.layout.quest_source_dialog_layout, null, false);
			final CheckBox checkBox = inner.findViewById(R.id.checkBoxDontShowAgain);

			AlertDialogBuilder alertDialogBuilder = new AlertDialogBuilder(activity);
			alertDialogBuilder
					.setTitle(R.string.quest_source_dialog_title)
					.setView(inner)
					.setPositiveButton(R.string.quest_generic_confirmation_yes, (dialog, which) ->
					{
						++timesShown;
						dontShowAgain = checkBox.isChecked();
						listener.onFindQuestSourceResult(SURVEY);
					})
					.setNegativeButton(android.R.string.cancel, null);

			checkBox.setVisibility(timesShown < 2 ? View.GONE : View.VISIBLE);

			alertDialogBuilder.show();
		}
	}

	private Double getDistanceToElementInMeters(long questId, QuestGroup group, Location[] locations)
	{
		try
		{
			List<Location> locationsList = asListWithoutNullsAndDuplicates(locations);
			Geometry locationGeometry = createLocationsGeometry(locationsList);
			if(locationGeometry == null) return null;
			double accuracy = getMeanAccuracy(locationsList);

			Geometry questGeometry = JTSConst.toGeometry(getQuestGeometry(questId, group));
			return Math.max(0, getDistanceInMeters(locationGeometry, questGeometry) - accuracy);
		}
		catch (RuntimeException e)
		{
			// if JTS throws any exception, it should not tear down the application. Instead, assume
			// that the distance can simply not determined (-> null)
			return null;
		}
	}

	private double getMeanAccuracy(List<Location> locations)
	{
		double accuracySum = 0;

		for(Location location : locations)
		{
			accuracySum += location.getAccuracy();
		}
		return locations.isEmpty() ? 0 : accuracySum / locations.size();
	}

	private Geometry createLocationsGeometry(List<Location> locations)
	{
		if(locations == null || locations.isEmpty()) return null;
		if(locations.size() == 1)
		{
			Location location = locations.get(0);
			return geometryFactory.createPoint(createCoordinate(location));
		}
		Coordinate[] coordinates = new Coordinate[locations.size()];
		for(int i=0; i<locations.size(); ++i)
		{
			Location location = locations.get(i);
			coordinates[i] = createCoordinate(location);
		}
		return geometryFactory.createLineString(coordinates);
	}

	private static Coordinate createCoordinate(Location location)
	{
		return new Coordinate(location.getLongitude(), location.getLatitude());
	}

	private static List<Location> asListWithoutNullsAndDuplicates(Location[] locations)
	{
		List<Location> result = new ArrayList<>(locations.length);
		Location previous = null;
		for (Location current : locations)
		{
			if (current == null) continue;
			if (previous != null)
			{
				if (previous.getLatitude() == current.getLatitude() &&
						previous.getLongitude() == current.getLongitude()) continue;
			}

			result.add(current);

			previous = current;
		}
		return result;
	}

	private static double getDistanceInMeters(Geometry one, Geometry two)
	{
		Coordinate[] nearest = DistanceOp.nearestPoints(one, two);
		return SphericalEarthMath.distance(
				JTSConst.toLatLon(nearest[0]), JTSConst.toLatLon(nearest[1]));
	}

	private ElementGeometry getQuestGeometry(long questId, QuestGroup group)
	{
		switch (group)
		{
			case OSM:
				return osmQuestDB.get(questId).getGeometry();
			case OSM_NOTE:
				return osmNoteQuestDao.get(questId).getGeometry();
		}
		return null;
	}
}
