package de.westnordost.streetcomplete.quests;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.view.ImageSelectAdapter;

/**
 * Abstract class for quests with a list of images and one to select.
 */

public abstract class ImageListQuestAnswerFragment extends AbstractQuestFormAnswerFragment {

	public static final String OSM_VALUES = "osm_values";

    private static final String
			SELECTED_INDICES = "selected_indices",
			EXPANDED = "expanded";

	protected ImageSelectAdapter imageSelector;
    private Button showMoreButton;
	private RecyclerView valueList;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View contentView = setContentView(R.layout.quest_generic_list);

		valueList = contentView.findViewById(R.id.listSelect);
        GridLayoutManager lm = new GridLayoutManager(getActivity(), getItemsPerRow());
        valueList.setLayoutManager(lm);
		valueList.setNestedScrollingEnabled(false);

		showMoreButton = view.findViewById(R.id.buttonShowMore);
		showMoreButton.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				List<ImageSelectAdapter.Item> all = Arrays.<ImageSelectAdapter.Item>asList(getItems());
				imageSelector.addItems(all.subList(imageSelector.getItemCount(), all.size()));
				showMoreButton.setVisibility(View.GONE);
			}
		});

		int selectableItems = getMaxSelectableItems();
		TextView selectHint = view.findViewById(R.id.selectHint);
		selectHint.setText(selectableItems == 1 ? R.string.quest_roofShape_select_one : R.string.quest_select_hint);

		imageSelector = new ImageSelectAdapter(selectableItems);

        return view;
    }


	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		int initiallyShow = getMaxNumberOfInitiallyShownItems();
		if(savedInstanceState != null)
		{
			if(savedInstanceState.getBoolean(EXPANDED)) initiallyShow = -1;
			showInitialItems(initiallyShow);

			List<Integer> selectedIndices = savedInstanceState.getIntegerArrayList(SELECTED_INDICES);
			imageSelector.selectIndices(selectedIndices);
		}
		else
		{
			showInitialItems(initiallyShow);
		}
		valueList.setAdapter(imageSelector);
	}

	protected int getItemsPerRow()
	{
		return 4;
	}
	/** return -1 for any number. Default: 1 */
    protected int getMaxSelectableItems()
	{
		return 1;
	}
	/** return -1 for showing all items at once. Default: -1 */
	protected int getMaxNumberOfInitiallyShownItems()
	{
		return -1;
	}
	protected abstract OsmItem[] getItems();

	private void showInitialItems(int initiallyShow)
	{
		List<ImageSelectAdapter.Item> all = Arrays.<ImageSelectAdapter.Item>asList(getItems());
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
		applyAnswer();
	}

	protected void applyAnswer()
	{
		Bundle answer = new Bundle();

		ArrayList<String> osmValues = new ArrayList<>();
		for(Integer selectedIndex : imageSelector.getSelectedIndices())
		{
			osmValues.add(getItems()[selectedIndex].osmValue);
		}
		if(!osmValues.isEmpty())
		{
			answer.putStringArrayList(OSM_VALUES, osmValues);
		}
		applyFormAnswer(answer);
	}

    @Override public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
		outState.putIntegerArrayList(SELECTED_INDICES, imageSelector.getSelectedIndices());
		outState.putBoolean(EXPANDED, showMoreButton.getVisibility() == View.GONE);
    }

    @Override public boolean hasChanges()
    {
        return !imageSelector.getSelectedIndices().isEmpty();
    }

    protected static class OsmItem extends ImageSelectAdapter.Item
    {
        public final String osmValue;

        public OsmItem(String osmValue, int drawableId, int titleId)
        {
            super(drawableId, titleId);
            this.osmValue = osmValue;
        }

        public OsmItem(String osmValue, int drawableId) {
            this(osmValue, drawableId, -1);
        }
    }
}
