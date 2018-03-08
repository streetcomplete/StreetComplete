package de.westnordost.streetcomplete.statistics;

import android.os.AsyncTask;
import android.widget.TextView;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;

public class UploadedAnswersCounter
{
	private final QuestStatisticsDao questStatisticsDB;

	private int solvedQuests;

	private TextView textView;

	@Inject public UploadedAnswersCounter(QuestStatisticsDao questStatisticsDB)
	{
		this.questStatisticsDB = questStatisticsDB;
	}

	public void setTarget(TextView textView) {
		this.textView = textView;
	}

	public void update()
	{
		new AsyncTask<Void, Void, Void>()
		{
			@Override protected Void doInBackground(Void... params)
			{
				solvedQuests = questStatisticsDB.getTotalAmount();
				return null;
			}

			@Override protected void onPostExecute(Void result)
			{
				updateText();
			}
		}.execute();
	}

	private void updateText() {
		textView.setText(String.valueOf(solvedQuests));
	}
}
