package de.westnordost.streetcomplete.quests;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Arrays;
import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.view.AbstractImageSelectAdapter;
import de.westnordost.streetcomplete.view.ImageSelectAdapter;

/**
 * Abstract class for quests with a list of images and one to select.
 */

public abstract class ImageListQuestAnswerFragment extends AbstractQuestFormAnswerFragment {

	public static final String OSM_VALUE = "osm_value";

    private static final String
			SELECTED_INDEX = "selected_item",
			EXPANDED = "expanded";

	private ImageSelectAdapter imageSelector;
    private Button showMoreButton;

	private int maxInitiallyShownItems;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View contentView = setContentView(R.layout.quest_generic_list);

		RecyclerView valueList = (RecyclerView) contentView.findViewById(R.id.listSelect);
        GridLayoutManager lm = new GridLayoutManager(getActivity(), 4);
        valueList.setLayoutManager(lm);
		valueList.setNestedScrollingEnabled(false);

		showMoreButton = (Button) view.findViewById(R.id.buttonShowMore);
		showMoreButton.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				List<AbstractImageSelectAdapter.Item> all = Arrays.<AbstractImageSelectAdapter.Item>asList(getItems());
				imageSelector.addItems(all.subList(imageSelector.getItemCount(), all.size()));
				showMoreButton.setVisibility(View.GONE);
			}
		});

		imageSelector = new ImageSelectAdapter();
		int initiallyShow = getMaxNumberOfInitiallyShownItems();
		if(savedInstanceState != null)
		{
			if(savedInstanceState.getBoolean(EXPANDED)) initiallyShow = -1;
			showInitialItems(initiallyShow);

			int index = savedInstanceState.getInt(SELECTED_INDEX, -1);
			if(index > -1) imageSelector.selectIndex(index);
		}
		else
		{
			showInitialItems(initiallyShow);
		}
		valueList.setAdapter(imageSelector);

        return view;
    }

    /** return -1 for showing all items at once */
	protected abstract int getMaxNumberOfInitiallyShownItems();
	protected abstract ListValue[] getItems();

	private void showInitialItems(int initiallyShow)
	{
		List<AbstractImageSelectAdapter.Item> all = Arrays.<AbstractImageSelectAdapter.Item>asList(getItems());
		if(initiallyShow == -1 || initiallyShow >= all.size())
		{
			imageSelector.setItems(all);
			showMoreButton.setVisibility(View.GONE);
		}
		else
		{
			imageSelector.setItems(all.subList(0, initiallyShow));
		}
	}

	@Override protected void onClickOk()
	{
		Bundle answer = new Bundle();
		Integer selectedIndex = imageSelector.getSelectedIndex();
		if(selectedIndex != -1)
		{
			String osmValue = getItems()[selectedIndex].osmValue;
			answer.putString(OSM_VALUE, osmValue);
		}
		applyFormAnswer(answer);
	}

    @Override public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_INDEX, imageSelector.getSelectedIndex());
		outState.putBoolean(EXPANDED, showMoreButton.getVisibility() == View.GONE);
    }

    @Override public boolean hasChanges()
    {
        return imageSelector.getSelectedIndex() != -1;
    }

    protected static class ListValue extends AbstractImageSelectAdapter.Item
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
