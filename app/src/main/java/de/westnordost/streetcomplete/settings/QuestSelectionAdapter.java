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

import java.util.Collections;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestType;
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderList;
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeDao;
import de.westnordost.streetcomplete.view.ListAdapter;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_DRAG;
import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_IDLE;
import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;
import static android.support.v7.widget.helper.ItemTouchHelper.UP;

public class QuestSelectionAdapter extends ListAdapter<QuestSelectionAdapter.QuestVisibility>
{
	private final VisibleQuestTypeDao visibleQuestTypeDao;
	private final QuestTypeOrderList questTypeOrderList;


	@Inject public QuestSelectionAdapter(
			VisibleQuestTypeDao visibleQuestTypeDao, QuestTypeOrderList questTypeOrderList)
	{
		super();
		this.visibleQuestTypeDao = visibleQuestTypeDao;
		this.questTypeOrderList = questTypeOrderList;
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

		@Override public void onCheckedChanged(final CompoundButton compoundButton, boolean b)
		{
			item.visible = b;
			updateSelectionStatus();
			visibleQuestTypeDao.setVisible(item.questType, item.visible);
			if(b && item.questType.getDefaultDisabledMessage() > 0)
			{
				new AlertDialogBuilder(compoundButton.getContext())
						.setTitle(R.string.enable_quest_confirmation_title)
						.setMessage(item.questType.getDefaultDisabledMessage())
						.setPositiveButton(android.R.string.yes, null)
						.setNegativeButton(android.R.string.no, (dialog, which) -> compoundButton.setChecked(false))
						.show();
			}
		}
	}
}
