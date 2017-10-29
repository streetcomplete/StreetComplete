package de.westnordost.streetcomplete.settings;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestType;
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeDao;
import de.westnordost.streetcomplete.view.ListAdapter;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_DRAG;
import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_IDLE;
import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;
import static android.support.v7.widget.helper.ItemTouchHelper.UP;

public class QuestVisibilityAdapter extends ListAdapter<QuestVisibilityAdapter.QuestVisibility>
{
	private final VisibleQuestTypeDao visibleQuestTypeDao;

	@Inject public QuestVisibilityAdapter(VisibleQuestTypeDao visibleQuestTypeDao,
										  QuestTypeRegistry questTypeRegistry)
	{
		super(createQuestTypeVisibilityList(questTypeRegistry.getAll(), visibleQuestTypeDao.getAll()));
		this.visibleQuestTypeDao = visibleQuestTypeDao;
	}

	@Override public void onAttachedToRecyclerView(RecyclerView recyclerView)
	{
		super.onAttachedToRecyclerView(recyclerView);
		ItemTouchHelper ith = new ItemTouchHelper(new TouchHelperCallback());
		ith.attachToRecyclerView(recyclerView);
	}

	private static List<QuestVisibility> createQuestTypeVisibilityList(
			List<QuestType> questTypes, List<QuestType> visibleQuestTypes)
	{
		List<QuestVisibility> list = new ArrayList<>(questTypes.size());
		for (QuestType questType : questTypes)
		{
			QuestVisibility questVisibility = new QuestVisibility();
			questVisibility.questType = questType;
			questVisibility.visible = visibleQuestTypes.contains(questType);
			list.add(questVisibility);
		}
		return list;
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
		@Override
		public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
		{
			return 0;
/*			QuestVisibility qv = ((QuestVisibilityViewHolder)viewHolder).item;
			if(!qv.isInteractionEnabled()) return 0;

			return makeFlag(ACTION_STATE_IDLE, UP | DOWN) | makeFlag(ACTION_STATE_DRAG, UP | DOWN);*/
		}

		@Override
		public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
		{
			QuestVisibility qv = ((QuestVisibilityViewHolder)target).item;
			if(!qv.isInteractionEnabled()) return false;

			Collections.swap(getList(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
			notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());



			return true;
		}

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
		private QuestVisibility item;

		public QuestVisibilityViewHolder(View itemView)
		{
			super(itemView);
			iconView = itemView.findViewById(R.id.imageView);
			textView = itemView.findViewById(R.id.textView);
			checkBox = itemView.findViewById(R.id.checkBox);
		}

		@Override protected void onBind(final QuestVisibility item)
		{
			this.item = item;
			int colorResId = item.isInteractionEnabled() ? android.R.color.white : R.color.colorGreyedOut;
			itemView.setBackgroundResource(colorResId);
			iconView.setImageResource(item.questType.getIcon());
			textView.setText(textView.getResources().getString(item.questType.getTitle(),"…"));
			checkBox.setOnCheckedChangeListener(null);
			checkBox.setChecked(item.visible);
			checkBox.setEnabled(item.isInteractionEnabled());
			checkBox.setOnCheckedChangeListener(this);

			updateSelectionStatus();
		}

		private void updateSelectionStatus()
		{
			if(!item.visible)
			{
				iconView.setColorFilter(itemView.getResources().getColor(R.color.colorGreyedOut));
			}
			else
			{
				iconView.clearColorFilter();
			}
			textView.setEnabled(item.visible);
		}

		@Override public void onCheckedChanged(CompoundButton compoundButton, boolean b)
		{
			item.visible = b;
			updateSelectionStatus();
			visibleQuestTypeDao.setVisible(item.questType, item.visible);
		}
	}
}
