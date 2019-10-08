package de.westnordost.streetcomplete.statistics;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import java.util.Arrays;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestSplitWayDao;
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteDao;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;

public class AnswersCounter
{
	private final OsmQuestDao questDB;
	private final OsmNoteQuestDao noteQuestDB;
	private final CreateNoteDao createNoteDB;
	private final OsmQuestSplitWayDao splitWayDB;
	private final QuestStatisticsDao questStatisticsDB;

	private int uploaded;
	private int unsynced;

	private TextView uploadedText;
	private TextView unsyncedText;
	private View unsyncedContainer;

	private boolean isFirstUpdateDone;
	private boolean isAutosync;

	@Inject public AnswersCounter(OsmQuestDao questDB, OsmNoteQuestDao noteQuestDB,
								  CreateNoteDao createNoteDB, OsmQuestSplitWayDao splitWayDB,
								  QuestStatisticsDao questStatisticsDB)
	{
		this.questDB = questDB;
		this.noteQuestDB = noteQuestDB;
		this.createNoteDB = createNoteDB;
		this.splitWayDB = splitWayDB;
		this.questStatisticsDB = questStatisticsDB;
	}

	public void setViews(TextView uploadedAnswersTextView, TextView unsyncedAnswersTextView,
						 View unsyncedContainer)
	{
		this.uploadedText = uploadedAnswersTextView;
		this.unsyncedText = unsyncedAnswersTextView;
		this.unsyncedContainer = unsyncedContainer;
	}

	public void setAutosync(boolean autosync)
	{
		isAutosync = autosync;
		unsyncedContainer.setVisibility(autosync ? View.GONE : View.VISIBLE);
		updateTexts();
	}

	public View getAnswerTarget()
	{
		return isAutosync ? uploadedText : unsyncedText;
	}

	public void addOneUnsynced(String source)
	{
		unsynced++;
		updateTexts();
	}

	public void subtractOneUnsynced(String source)
	{
		unsynced--;
		updateTexts();
	}

	public void uploadedOne()
	{
		unsynced--;
		uploaded++;
		updateTexts();
	}

	public void discardedOne()
	{
		unsynced--;
		updateTexts();
	}

	public Integer waitingForUpload(){
		return unsynced;
	}

	public Integer uploaded(){
		return unsynced;
	}

	@SuppressLint("StaticFieldLeak") public void update()
	{
		new AsyncTask<Void, Void, Void>()
		{
			@Override protected Void doInBackground(Void... params)
			{
				uploaded = questStatisticsDB.getTotalAmount();
				unsynced = 0;
				unsynced += questDB.getCount(Arrays.asList(QuestStatus.ANSWERED), null, null, null, null);
				unsynced += noteQuestDB.getCount(Arrays.asList(QuestStatus.ANSWERED), null, null);
				unsynced += splitWayDB.getCount();
				unsynced += createNoteDB.getCount();
				return null;
			}

			@Override protected void onPostExecute(Void result)
			{
				updateTexts();
				isFirstUpdateDone = true;
			}
		}.execute();
	}


	private void updateTexts()
	{
		if(isAutosync)
		{
			updateText(uploadedText, uploaded + unsynced);
		}
		else
		{
			updateText(uploadedText, uploaded);
			updateText(unsyncedText, unsynced);
		}
	}

	private void updateText(TextView view, int value)
	{
		if(isFirstUpdateDone) try
		{
			int previous = Integer.parseInt(view.getText().toString());
			if(previous < value) animateChange(view, 1.6f);
			// not important to highlight that and looks better IMO if only the positive changes are animated
			//else if(previous > value) animateChange(view, 0.6f);
		}
		catch (NumberFormatException ignore) { }
		view.setText(String.valueOf(value));
	}

	private void animateChange(View view, float scale)
	{
		view.animate()
			.scaleX(scale).scaleY(scale)
			.setInterpolator(new DecelerateInterpolator(2f))
			.setDuration(100)
			.withEndAction(() ->
			{
				view.animate()
					.scaleX(1).scaleY(1)
					.setInterpolator(new AccelerateDecelerateInterpolator())
					.setDuration(100);
			});
	}
}
