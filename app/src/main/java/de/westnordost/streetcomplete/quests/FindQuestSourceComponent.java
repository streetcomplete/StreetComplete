package de.westnordost.streetcomplete.quests;

import android.app.Activity;
import android.content.DialogInterface;
import android.location.Location;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

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
	private static final float MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY = 50; //m

	private static final String
			SURVEY = "survey",
			LOCAL_KNOWLEGE = "local knowledge";

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

	public void findSource(final long questId, final QuestGroup group, final Location location,
						   final Listener listener)
	{
		Double distance = getDistanceToElement(questId, group, location);
		if(distance != null && distance < MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY)
		{
			listener.onFindQuestSourceResult(SURVEY);
		}
		else
		{
			AlertDialogBuilder alertDialogBuilder = new AlertDialogBuilder(activity);
			alertDialogBuilder
					.setTitle(R.string.quest_source_dialog_title)
					.setMessage(R.string.quest_source_dialog_message)
					.setPositiveButton(R.string.quest_source_dialog_button_visited_before, new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							listener.onFindQuestSourceResult(LOCAL_KNOWLEGE);
						}
					});

			if(distance == null)
			{
				alertDialogBuilder.setNeutralButton(R.string.quest_source_dialog_button_on_site, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						listener.onFindQuestSourceResult(SURVEY);
					}
				});
			}
			else
			{
				alertDialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						// nothing
					}
				});
			}
			alertDialogBuilder.show();
		}
	}

	private Double getDistanceToElement(long questId, QuestGroup group, Location location)
	{
		if(location == null) return null;
		try
		{
			Geometry locationGeometry = geometryFactory.createPoint(new Coordinate(location.getLongitude(), location.getLatitude()));
			Geometry questGeometry = JTSConst.toGeometry(getQuestGeometry(questId, group));

			double degreeDistance = questGeometry.distance(locationGeometry);
			double distanceInMeters = SphericalEarthMath.EARTH_RADIUS * Math.PI * degreeDistance / 180;
			return Math.max(0, distanceInMeters - location.getAccuracy());
		}
		catch (RuntimeException e)
		{
			// if JTS throws any exception, it should not tear down the application. Instead, assume
			// that the distance can simply not determined (-> null)
			return null;
		}
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
