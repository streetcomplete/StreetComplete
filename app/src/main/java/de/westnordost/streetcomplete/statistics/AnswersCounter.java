package de.westnordost.streetcomplete.statistics;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.quest.UnsyncedChangesDao;
import de.westnordost.streetcomplete.data.user.QuestStatisticsDao;

public class AnswersCounter
{
	private final UnsyncedChangesDao unsyncedChangesDB;
	private final QuestStatisticsDao questStatisticsDB;

	private int uploaded;
	private int unsynced;

	private TextView uploadedText;
	private TextView unsyncedText;
	private View unsyncedContainer;

	private boolean isFirstUpdateDone;
	private boolean isAutosync;

	@Inject public AnswersCounter(UnsyncedChangesDao unsyncedChangesDB,
								  QuestStatisticsDao questStatisticsDB)
	{
		this.unsyncedChangesDB = unsyncedChangesDB;
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
				unsynced = unsyncedChangesDB.getCount();
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
