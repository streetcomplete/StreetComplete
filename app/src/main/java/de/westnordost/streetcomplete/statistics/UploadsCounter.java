package de.westnordost.streetcomplete.statistics;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;

public class UploadsCounter extends android.support.v7.widget.AppCompatTextView {

    @Inject
    OsmQuestDao questDB;

    private int answeredQuests;

    public UploadsCounter(Context context, AttributeSet attrs) {
        super(context, attrs);
        Injector.instance.getApplicationComponent().inject(this);
    }

    public void answeredQuest(String source) {
        answeredQuests++;
        updateText();
    }

    public void undidQuest(String source) {
        answeredQuests--;
        updateText();
    }

    public void update() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                answeredQuests = questDB.getCount(null, QuestStatus.ANSWERED);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                updateText();
            }
        }.execute();
    }

    private void updateText() {
        String text = "0";
        if (answeredQuests < 0) {
            text += answeredQuests;
        } else if (answeredQuests > 0) {
            text += "+" + answeredQuests;
        }

        setText(text);
    }
}
