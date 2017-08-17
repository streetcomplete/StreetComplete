package de.westnordost.streetcomplete.statistics;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;

public class AnswersCounter extends android.support.v7.widget.AppCompatTextView
{
	@Inject QuestStatisticsDao questStatisticsDB;
	@Inject OsmQuestDao questDB;

	private int answeredQuests;
	private int solvedQuests;

	public AnswersCounter(Context context)
	{
		super(context);
		init();
	}

	public AnswersCounter(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public AnswersCounter(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init()
	{
		Injector.instance.getApplicationComponent().inject(this);
	}

	public void answeredQuest(String source)
	{
		answeredQuests++;
		updateText();
	}

	public void undidQuest(String source)
	{
		answeredQuests--;
		updateText();
	}

	public void update()
	{
		new AsyncTask<Void, Void, Void>()
		{
			@Override protected Void doInBackground(Void... params)
			{
				answeredQuests = questDB.getCount(null, QuestStatus.ANSWERED);
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
		String text = "" + solvedQuests;
		if(answeredQuests < 0) text += " (" + answeredQuests + ")";
		else if(answeredQuests > 0) text += " (+" + answeredQuests + ")";

		// min ems = number of digits in solved quests plus the "(+XX)" for answered quests
		setMinEms((int) Math.floor(Math.log10(solvedQuests)) + 4 + 2);
		setText(text);
	}
}
