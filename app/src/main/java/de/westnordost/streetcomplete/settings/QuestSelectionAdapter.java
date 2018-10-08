package de.westnordost.streetcomplete.settings;

import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.FutureTask;

import javax.inject.Inject;

import de.westnordost.countryboundaries.CountryBoundaries;
import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestType;
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderList;
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeDao;
import de.westnordost.streetcomplete.view.ListAdapter;


import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_DRAG;
import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_IDLE;
import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;
import static android.support.v7.widget.helper.ItemTouchHelper.UP;

public class QuestSelectionAdapter extends ListAdapter<QuestSelectionAdapter.QuestVisibility>
{
	private final VisibleQuestTypeDao visibleQuestTypeDao;
	private final QuestTypeOrderList questTypeOrderList;
	private final List<String> currentCountryCodes;

	@Inject public QuestSelectionAdapter(
		VisibleQuestTypeDao visibleQuestTypeDao, QuestTypeOrderList questTypeOrderList,
		FutureTask<CountryBoundaries> countryBoundaries, SharedPreferences prefs)
	{
		super();
		this.visibleQuestTypeDao = visibleQuestTypeDao;
		this.questTypeOrderList = questTypeOrderList;

		double lat = Double.longBitsToDouble(prefs.getLong(Prefs.MAP_LATITUDE, Double.doubleToLongBits(0)));
		double lng = Double.longBitsToDouble(prefs.getLong(Prefs.MAP_LONGITUDE, Double.doubleToLongBits(0)));
		try	{ currentCountryCodes = countryBoundaries.get().getIds(lng, lat); }
		catch (Exception e)	{ throw new RuntimeException(e);	}
	}

	@Override public void onAttachedToRecyclerView(RecyclerView recyclerView)
	{
		super.onAttachedToRecyclerView(recyclerView);
		ItemTouchHelper ith = new ItemTouchHelper(new TouchHelperCallback());
		ith.attachToRecyclerView(recyclerView);
	}

	@Override
	public ListAdapter.ViewHolder<QuestVisibility> onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View layout = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.row_quest_selection, parent, false);
		return new QuestVisibilityViewHolder(layout);
	}

	private class TouchHelperCallback extends ItemTouchHelper.Callback
	{
		private int draggedFrom = -1, draggedTo = -1;

		@Override
		public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
		{
			QuestVisibility qv = ((QuestVisibilityViewHolder)viewHolder).item;
			if(!qv.isInteractionEnabled()) return 0;

			return makeFlag(ACTION_STATE_IDLE, UP | DOWN) | makeFlag(ACTION_STATE_DRAG, UP | DOWN);
		}

		@Override
		public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
		{
			int from = viewHolder.getAdapterPosition();
			int to = target.getAdapterPosition();
			Collections.swap(getList(), from, to);
			notifyItemMoved(from, to);
			return true;
		}

		@Override
		public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target)
		{
			QuestVisibility qv = ((QuestVisibilityViewHolder)target).item;
			return qv.isInteractionEnabled();
		}

		@Override
		public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y)
		{
			super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
			if(draggedFrom == -1) draggedFrom = fromPos;
			draggedTo = toPos;
		}

		@Override public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState)
		{
			super.onSelectedChanged(viewHolder, actionState);
			if(actionState == ACTION_STATE_IDLE && draggedTo != draggedFrom)
			{
				int pos = draggedTo;
				if(draggedTo == 0) pos++;

				QuestType before = getList().get(pos-1).questType;
				QuestType after = getList().get(pos).questType;

				questTypeOrderList.apply(before, after);

				draggedTo = draggedFrom = -1;
			}
		}

		@Override public boolean isItemViewSwipeEnabled() { return false; }

		@Override public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) { }
	}

	public static class QuestVisibility
	{
		public QuestType questType;
		public boolean visible;

		public boolean isInteractionEnabled()
		{
			return !(questType instanceof OsmNoteQuestType);
		}
	}

	private class QuestVisibilityViewHolder extends ListAdapter.ViewHolder<QuestVisibility> implements CompoundButton.OnCheckedChangeListener
	{
		private ImageView iconView;
		private TextView textView;
		private CheckBox checkBox;
		private TextView textCountryDisabled;
		private QuestVisibility item;

		public QuestVisibilityViewHolder(View itemView)
		{
			super(itemView);
			iconView = itemView.findViewById(R.id.imageView);
			textView = itemView.findViewById(R.id.textView);
			checkBox = itemView.findViewById(R.id.checkBox);
			textCountryDisabled = itemView.findViewById(R.id.textCountryDisabled);
		}

		@Override protected void onBind(final QuestVisibility item)
		{
			this.item = item;
			int colorResId = item.isInteractionEnabled() ? android.R.color.white : R.color.greyed_out;
			itemView.setBackgroundResource(colorResId);
			iconView.setImageResource(item.questType.getIcon());
			textView.setText(textView.getResources().getString(item.questType.getTitle(),"…"));
			checkBox.setOnCheckedChangeListener(null);
			checkBox.setChecked(item.visible);
			checkBox.setEnabled(item.isInteractionEnabled());
			checkBox.setOnCheckedChangeListener(this);

			if(!isEnabledInCurrentCountry())
			{
				String cc = currentCountryCodes.isEmpty() ? "Atlantis" : currentCountryCodes.get(0);
				textCountryDisabled.setText(String.format(
					textCountryDisabled.getResources().getString(R.string.questList_disabled_in_country),
					new Locale("", cc).getDisplayCountry()
				));
				textCountryDisabled.setVisibility(View.VISIBLE);
			}
			else
			{
				textCountryDisabled.setVisibility(View.GONE);
			}

			updateSelectionStatus();
		}

		private boolean isEnabledInCurrentCountry()
		{
			if(item.questType instanceof OsmElementQuestType)
			{
				OsmElementQuestType questType = (OsmElementQuestType) item.questType;
				Countries countries = questType.getEnabledForCountries();
				for(String currentCountryCode : currentCountryCodes)
				{
					if(countries.getExceptions().contains(currentCountryCode))
					{
						return !countries.isAllExcept();
					}
				}
				return countries.isAllExcept();
			}
			return true;
		}

		private void updateSelectionStatus()
		{
			if(!item.visible)
			{
				iconView.setColorFilter(itemView.getResources().getColor(R.color.greyed_out));
			}
			else
			{
				iconView.clearColorFilter();
			}
			textView.setEnabled(item.visible);
		}

		@Override public void onCheckedChanged(final CompoundButton compoundButton, boolean b)
		{
			item.visible = b;
			updateSelectionStatus();
			visibleQuestTypeDao.setVisible(item.questType, item.visible);
			if(b && item.questType.getDefaultDisabledMessage() > 0)
			{
				new AlertDialog.Builder(compoundButton.getContext())
						.setTitle(R.string.enable_quest_confirmation_title)
						.setMessage(item.questType.getDefaultDisabledMessage())
						.setPositiveButton(android.R.string.yes, null)
						.setNegativeButton(android.R.string.no, (dialog, which) -> compoundButton.setChecked(false))
						.show();
			}
		}
	}
}
