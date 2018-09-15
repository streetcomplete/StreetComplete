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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.view.ImageSelectAdapter;
import de.westnordost.streetcomplete.view.Item;

/**
 * Abstract class for quests with a list of images and one or several to select.
 */
public abstract class ImageListQuestAnswerFragment extends AbstractQuestFormAnswerFragment {

	public static final String OSM_VALUES = "osm_values";

    private static final String
			SELECTED_INDICES = "selected_indices",
			EXPANDED = "expanded";

	protected ImageSelectAdapter imageSelector;
    private Button showMoreButton;
	private RecyclerView valueList;

	private List<Item> allItems;

	@Inject LastPickedValuesStore favs;

	@Override public void onCreate(Bundle inState)
	{
		super.onCreate(inState);
		Injector.instance.getApplicationComponent().inject(this);
		allItems = Collections.unmodifiableList(Arrays.asList(getItems()));
	}

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View contentView = setContentView(R.layout.quest_generic_list);

		valueList = contentView.findViewById(R.id.listSelect);
        GridLayoutManager lm = new GridLayoutManager(getActivity(), getItemsPerRow());
        valueList.setLayoutManager(lm);
		valueList.setNestedScrollingEnabled(false);

		showMoreButton = contentView.findViewById(R.id.buttonShowMore);
		showMoreButton.setOnClickListener(v ->
		{
			imageSelector.setItems(getItems(-1));
			showMoreButton.setVisibility(View.GONE);
		});

		int selectableItems = getMaxSelectableItems();
		TextView selectHint = contentView.findViewById(R.id.selectHint);
		selectHint.setText(selectableItems == 1 ? R.string.quest_roofShape_select_one : R.string.quest_select_hint);

		imageSelector = new ImageSelectAdapter(selectableItems);
		imageSelector.addOnItemSelectionListener(new ImageSelectAdapter.OnItemSelectionListener()
		{
			@Override public void onIndexSelected(int index) { checkIsFormComplete(); }
			@Override public void onIndexDeselected(int index) { checkIsFormComplete(); }
		});

        return view;
    }

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		int initiallyShow = getMaxNumberOfInitiallyShownItems();
		if(savedInstanceState != null)
		{
			if(savedInstanceState.getBoolean(EXPANDED)) initiallyShow = -1;
			showItems(initiallyShow);

			List<Integer> selectedIndices = savedInstanceState.getIntegerArrayList(SELECTED_INDICES);
			imageSelector.select(selectedIndices);
		}
		else
		{
			showItems(initiallyShow);
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
	protected abstract Item[] getItems();

	@Override protected void onClickOk()
	{
		applyAnswer();
	}

	protected void applyAnswer()
	{
		Bundle answer = new Bundle();

		ArrayList<String> osmValues = new ArrayList<>();
		for(Item item : imageSelector.getSelectedItems())
		{
			osmValues.add(item.value);
		}
		if(!osmValues.isEmpty())
		{
			answer.putStringArrayList(OSM_VALUES, osmValues);
		}
		favs.addLastPicked(getClass().getSimpleName(), osmValues);
		applyAnswer(answer);
	}

    @Override public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
		outState.putIntegerArrayList(SELECTED_INDICES, imageSelector.getSelectedIndices());
		outState.putBoolean(EXPANDED, showMoreButton.getVisibility() == View.GONE);
    }

    @Override public boolean isFormComplete()
    {
        return !imageSelector.getSelectedIndices().isEmpty();
    }

	private void showItems(int initiallyShow)
	{
		if(initiallyShow == -1 || initiallyShow >= allItems.size())
		{
			showMoreButton.setVisibility(View.GONE);
		}

		imageSelector.setItems(getItems(initiallyShow));
	}

	private List<Item> getItems(int showOnly)
	{
		LinkedList<Item> items;
		if(showOnly == -1 || showOnly >= allItems.size())
		{
			items = new LinkedList<>(allItems);
		}
		else
		{
			items = new LinkedList<>(allItems.subList(0, showOnly));
		}

		if(allItems.size() > getItemsPerRow())
		{
			favs.moveLastPickedToFront(getClass().getSimpleName(), items, allItems);
		}
		return items;
	}
}
