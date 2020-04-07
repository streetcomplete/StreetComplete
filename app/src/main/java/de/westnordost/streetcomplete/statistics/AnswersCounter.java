package de.westnordost.streetcomplete.statistics;

import android.os.AsyncTask;
import android.view.View;

import javax.inject.Inject;

import de.westnordost.streetcomplete.controls.AnswersCounterView;
import de.westnordost.streetcomplete.controls.UploadButton;
import de.westnordost.streetcomplete.data.quest.UnsyncedChangesCountSource;
import de.westnordost.streetcomplete.data.user.QuestStatisticsDao;

public class AnswersCounter
{
	private final UnsyncedChangesCountSource unsyncedChangesDB;
	private final QuestStatisticsDao questStatisticsDB;

	private int uploaded;
	private int unsynced;

	private UploadButton uploadButton;
	private AnswersCounterView answersCounterView;

	private boolean isAutosync;

	@Inject public AnswersCounter(UnsyncedChangesCountSource unsyncedChangesDB,
								  QuestStatisticsDao questStatisticsDB)
	{
		this.unsyncedChangesDB = unsyncedChangesDB;
		this.questStatisticsDB = questStatisticsDB;
	}

	public void setViews(UploadButton uploadButton, AnswersCounterView answersCounterView)
	{
		this.uploadButton = uploadButton;
		this.answersCounterView = answersCounterView;
	}

	public void setAutosync(boolean autosync)
	{
		isAutosync = autosync;
		uploadButton.setVisibility(autosync ? View.INVISIBLE : View.VISIBLE);
		updateTexts();
	}

	public View getAnswerTarget()
	{
		return isAutosync ? answersCounterView : uploadButton;
	}

	public void addOneUnsynced()
	{
		unsynced++;
		updateTexts();
	}

	public void subtractOneUnsynced()
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

	// should instead query himself...
	@Deprecated
	public Integer waitingForUpload(){
		return unsynced;
	}

	public void update()
	{
		new AsyncTask<Void, Void, Void>()
		{
			@Override protected Void doInBackground(Void... params)
			{
				uploaded = questStatisticsDB.getTotalAmount();
				unsynced = unsyncedChangesDB.getCount();
				return null;
			}

			@Override protected void onPostExecute(Void result)
			{
				updateTexts();
			}
		}.execute();
	}


	private void updateTexts()
	{
		if(isAutosync)
		{
			answersCounterView.setUploadedCount(uploaded + unsynced);
		}
		else
		{
			answersCounterView.setUploadedCount(uploaded);
			uploadButton.setUploadableCount(unsynced);
		}
	}
}
