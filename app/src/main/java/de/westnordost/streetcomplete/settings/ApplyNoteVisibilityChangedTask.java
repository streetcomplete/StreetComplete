package de.westnordost.streetcomplete.settings;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.preference.Preference;
import java.lang.ref.WeakReference;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuest;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao;

public class ApplyNoteVisibilityChangedTask extends AsyncTask<Void, Void, Void>
{
	private final OsmNoteQuestDao osmNoteQuestDao;
	private final SharedPreferences prefs;

	private WeakReference<Preference> preference;

	@Inject public ApplyNoteVisibilityChangedTask(
			OsmNoteQuestDao osmNoteQuestDao, SharedPreferences prefs)
	{
		this.osmNoteQuestDao = osmNoteQuestDao;
		this.prefs = prefs;
	}

	@Override protected void onPreExecute()
	{
		Preference pref = preference.get();
		if(pref != null) pref.setEnabled(false);
	}

	@Override protected Void doInBackground(Void... voids)
	{
		boolean showNonQuestionNotes = prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false);
		for(OsmNoteQuest quest : osmNoteQuestDao.getAll(null,null))
		{
			if (quest.getStatus() == QuestStatus.NEW || quest.getStatus() == QuestStatus.INVISIBLE)
			{
				boolean visible = quest.probablyContainsQuestion() || showNonQuestionNotes;
				QuestStatus newQuestStatus = visible ? QuestStatus.NEW : QuestStatus.INVISIBLE;

				if (quest.getStatus() != newQuestStatus)
				{
					quest.setStatus(newQuestStatus);
					osmNoteQuestDao.update(quest);
				}
			}
		}
		return null;
	}

	@Override protected void onPostExecute(Void result)
	{
		Preference pref = preference.get();
		if(pref != null) pref.setEnabled(true);
	}

	public void setPreference(Preference preference)
	{
		this.preference = new WeakReference<>(preference);
	}
}
