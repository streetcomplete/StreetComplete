package de.westnordost.streetcomplete.quests.crossing_type;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;

public class AddCrossingTypeForm extends ImageListQuestAnswerFragment
{
    private final OsmItem[] TYPES = new OsmItem[] {
            new OsmItem("traffic_signals", R.drawable.crossing_type_signals, R.string.quest_crossing_type_signals),
            new OsmItem("uncontrolled", R.drawable.crossing_type_zebra, R.string.quest_crossing_type_uncontrolled),
            new OsmItem("unmarked", R.drawable.crossing_type_unmarked, R.string.quest_crossing_type_unmarked)
    };

    @Override protected OsmItem[] getItems()
    {
        return TYPES;
    }

    @Override protected int getItemsPerRow()
    {
        return 3;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setTitle(R.string.quest_crossing_type_title);
        return view;
    }
}
