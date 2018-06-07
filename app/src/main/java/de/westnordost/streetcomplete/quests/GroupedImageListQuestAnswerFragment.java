package de.westnordost.streetcomplete.quests;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.view.GroupedImageSelectAdapter;
import de.westnordost.streetcomplete.view.Item;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

/**
 * Abstract class for quests with a grouped list of images and one to select.
 *
 * Saving and restoring state is not implemented
 */
public abstract class GroupedImageListQuestAnswerFragment extends AbstractQuestFormAnswerFragment
{
	public static final String OSM_VALUE = "osm_value";

	private GroupedImageSelectAdapter imageSelector;
	private Button showMoreButton;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View contentView = setContentView(R.layout.quest_generic_list);

		RecyclerView valueList = contentView.findViewById(R.id.listSelect);
		GridLayoutManager lm = new GridLayoutManager(getActivity(), getItemsPerRow());
		valueList.setLayoutManager(lm);
		valueList.setNestedScrollingEnabled(false);

		showMoreButton = contentView.findViewById(R.id.buttonShowMore);
		showMoreButton.setOnClickListener(v ->
		{
			imageSelector.setItems(Arrays.asList(getAllItems()));
			showMoreButton.setVisibility(View.GONE);
		});

		TextView selectHint = contentView.findViewById(R.id.selectHint);
		selectHint.setText(R.string.quest_select_hint_most_specific);

		imageSelector = new GroupedImageSelectAdapter(lm);
		imageSelector.setItems(Arrays.asList(getTopItems()));

		valueList.setAdapter(imageSelector);

		return view;
	}

	@Override protected void onClickOk()
	{
		Item item = getSelectedItem();
		if(item != null && item.isGroup())
		{
			new AlertDialogBuilder(getContext())
				.setMessage(R.string.quest_generic_item_confirmation)
				.setNegativeButton(R.string.quest_generic_confirmation_no, null)
				.setPositiveButton(R.string.quest_generic_confirmation_yes,
					(dialog, which) -> applyAnswer())
				.show();
		}
		else
		{
			applyAnswer();
		}
	}

	private void applyAnswer()
	{
		Bundle answer = new Bundle();
		Item item = getSelectedItem();
		if(item != null)
		{
			answer.putString(OSM_VALUE, item.value);
		}
		applyFormAnswer(answer);
	}

	@Override public boolean hasChanges() { return getSelectedItem() != null; }

	private Item getSelectedItem() { return imageSelector.getSelectedItem(); }

	protected int getItemsPerRow() { return 3; }

	protected abstract Item[] getTopItems();
	protected abstract Item[] getAllItems();
}
