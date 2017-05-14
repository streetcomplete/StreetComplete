package de.westnordost.streetcomplete.quests;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.view.ImageSelectAdapter;

/**
 * Abstract class for quests with a list of images and one to select.
 */

public abstract class ImageListQuestAnswerFragment extends AbstractQuestFormAnswerFragment {

    protected static final String SELECTED_INDEX = "selected_item";

    protected static final int MORE_THAN_95_PERCENT_COVERED = 8;

    protected ImageSelectAdapter imageSelector;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View contentView = setContentView(R.layout.quest_generic_list);

        final RecyclerView valueList = (RecyclerView) contentView.findViewById(R.id.listSelect);
        GridLayoutManager lm = new GridLayoutManager(getActivity(), 4);
        valueList.setLayoutManager(lm);

        imageSelector = new ImageSelectAdapter();
        if(savedInstanceState != null)
        {
            imageSelector.setSelectedIndex(savedInstanceState.getInt(SELECTED_INDEX, -1));
        }
        valueList.setAdapter(imageSelector);
        valueList.setNestedScrollingEnabled(false);

        return view;
    }

    @Override public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_INDEX, imageSelector.getSelectedIndex());
    }

    @Override public boolean hasChanges()
    {
        return imageSelector.getSelectedIndex() != -1;
    }

    protected static class ListValue extends ImageSelectAdapter.Item
    {
        public final String osmValue;

        public ListValue(String osmValue, int drawableId, int titleId)
        {
            super(drawableId, titleId);
            this.osmValue = osmValue;
        }

        public ListValue(String osmValue, int drawableId) {
            this(osmValue, drawableId, -1);
        }
    }
}
