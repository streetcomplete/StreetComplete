package de.westnordost.streetcomplete.quests;

import android.app.Activity;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import java.util.Collections;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao;
import de.westnordost.streetcomplete.util.FlattenIterable;
import de.westnordost.streetcomplete.util.SphericalEarthMath;


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
		if(dontShowAgain || isWithinSurveyDistance(questId, group, location))
		{
			listener.onFindQuestSourceResult(SURVEY);
		}
		else
		{
			View inner = LayoutInflater.from(activity).inflate(
					R.layout.quest_source_dialog_layout, null, false);
			final CheckBox checkBox = inner.findViewById(R.id.checkBoxDontShowAgain);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
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

	private boolean isWithinSurveyDistance(long questId, QuestGroup group, Location location)
	{
		ElementGeometry geometry = getQuestGeometry(questId, group);
		if(geometry == null) return false;

		LatLon loc = new OsmLatLon(location.getLatitude(), location.getLongitude());

		FlattenIterable<LatLon> itb = new FlattenIterable<>(LatLon.class);
		if(geometry.polygons != null) itb.add(geometry.polygons);
		else if(geometry.polylines != null) itb.add(geometry.polylines);
		else itb.add(Collections.singleton(geometry.center));
		for (LatLon pos : itb)
		{
			double distance = SphericalEarthMath.distance(loc, pos);
			if(distance < location.getAccuracy() + MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY) return true;
		}

		return false;
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
