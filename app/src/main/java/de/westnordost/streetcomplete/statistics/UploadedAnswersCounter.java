package de.westnordost.streetcomplete.statistics;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;

public class UploadedAnswersCounter extends android.support.v7.widget.AppCompatTextView
{
	@Inject QuestStatisticsDao questStatisticsDB;

	private int solvedQuests;

	public UploadedAnswersCounter(Context context)
	{
		super(context);
		init();
	}

	public UploadedAnswersCounter(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public UploadedAnswersCounter(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init()
	{
		Injector.instance.getApplicationComponent().inject(this);
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

	private void updateText()
	{
		setText(String.valueOf(solvedQuests));
	}
}
