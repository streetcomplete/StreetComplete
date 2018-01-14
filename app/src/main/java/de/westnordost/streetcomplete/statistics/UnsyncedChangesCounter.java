package de.westnordost.streetcomplete.statistics;

import android.os.AsyncTask;
import android.widget.TextView;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteDao;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao;

public class UnsyncedChangesCounter {

    private final OsmQuestDao questDB;
    private final OsmNoteQuestDao noteQuestDB;
    private final CreateNoteDao createNoteDB;

    private TextView textView;

    private int changes;

    @Inject public UnsyncedChangesCounter(OsmQuestDao questDB, OsmNoteQuestDao noteQuestDB,
                                          CreateNoteDao createNoteDB) {
        this.questDB = questDB;
        this.noteQuestDB = noteQuestDB;
        this.createNoteDB = createNoteDB;
    }

    public void setTarget(TextView textView) {
        this.textView = textView;
    }

    public void increase(String source) {
        changes++;
        updateText();
    }

    public void decrement(String source) {
        changes--;
        updateText();
    }

    public void update() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                changes = 0;
                changes += questDB.getCount(null, QuestStatus.ANSWERED);
                changes += noteQuestDB.getCount(null, QuestStatus.ANSWERED);
                changes += createNoteDB.getCount();
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                updateText();
            }
        }.execute();
    }

    private void updateText() {
        textView.setText(String.valueOf(changes));
    }
}
