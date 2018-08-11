package de.westnordost.streetcomplete.statistics;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteDao;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;

public class AnswersCounter
{
	private final OsmQuestDao questDB;
	private final OsmNoteQuestDao noteQuestDB;
	private final CreateNoteDao createNoteDB;
	private final QuestStatisticsDao questStatisticsDB;

	private int uploaded;
	private int unsynced;

	private TextView uploadedText;
	private TextView unsyncedText;
	private View uploadedContainer;
	private View unsyncedContainer;

	private boolean isFirstUpdateDone;
	private boolean isAutosync;

	@Inject public AnswersCounter(OsmQuestDao questDB, OsmNoteQuestDao noteQuestDB,
								  CreateNoteDao createNoteDB, QuestStatisticsDao questStatisticsDB)
	{
		this.questDB = questDB;
		this.noteQuestDB = noteQuestDB;
		this.createNoteDB = createNoteDB;
		this.questStatisticsDB = questStatisticsDB;
	}

	public void setViews(TextView uploadedAnswersTextView, View uploadedContainer,
						 TextView unsyncedAnswersTextView, View unsyncedContainer)
	{
		this.uploadedText = uploadedAnswersTextView;
		this.unsyncedText = unsyncedAnswersTextView;
		this.uploadedContainer = uploadedContainer;
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

	public void increase(String source)
	{
		unsynced++;
		updateTexts();
	}

	public void decrement(String source)
	{
		unsynced--;
		updateTexts();
	}

	@SuppressLint("StaticFieldLeak") public void update()
	{
		new AsyncTask<Void, Void, Void>()
		{
			@Override protected Void doInBackground(Void... params)
			{
				uploaded = questStatisticsDB.getTotalAmount();
				unsynced = 0;
				unsynced += questDB.getCount(null, QuestStatus.ANSWERED);
				unsynced += noteQuestDB.getCount(null, QuestStatus.ANSWERED);
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
			updateText(uploadedText, uploadedContainer, uploaded + unsynced);
		}
		else
		{
			updateText(uploadedText, uploadedContainer, uploaded);
			updateText(unsyncedText, unsyncedContainer, unsynced);
		}
	}

	private void updateText(TextView view, View container, int value)
	{
		if(isFirstUpdateDone) try
		{
			int previous = Integer.parseInt(view.getText().toString());
			if(previous < value) animateChange(container, 1.6f);
			else if(previous > value) animateChange(container, 0.6f);
		}
		catch (NumberFormatException ignore) { }
		view.setText(String.valueOf(value));
	}

	private void animateChange(View view, float scale)
	{
		ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(view,
			PropertyValuesHolder.ofFloat(View.SCALE_X, scale),
			PropertyValuesHolder.ofFloat(View.SCALE_Y, scale));
		anim.setRepeatCount(1);
		anim.setRepeatMode(ValueAnimator.REVERSE);
		anim.setInterpolator(new DecelerateInterpolator(2f));
		anim.setDuration(150);
		anim.start();
	}

}
